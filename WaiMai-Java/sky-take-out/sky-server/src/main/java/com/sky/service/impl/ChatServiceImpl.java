
package com.sky.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.AiProperties;
import com.sky.rag.ChatMessage;
import com.sky.rag.DocVector;
import com.sky.rag.KnowledgeDoc;
import com.sky.service.ChatService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.QianwenAPI;
import com.sky.vo.ChatResponseVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    @Autowired
    private QianwenAPI qianwenAPI;
    @Autowired private RedisTemplate redisTemplate;
    @Autowired private AiProperties aiProperties;

    private static List<KnowledgeDoc> knowledgeBase;// 知识库对象列表
    private static final String DOC_KEY_PREFIX = "rag:doc:";//redis文档存储前缀
    private static final String HISTORY_KEY_PREFIX = "chat:history:";//redis聊天记录存储前缀
    private static final double SIM_THRESHOLD = 0.3;//相似阈值,大于算匹配
    private static final int RETRIEVE_TOP_K = 5;//返回top5
    private static final int EMBED_TIMEOUT_MS = 10000;//嵌入接口超时
    private static final int CHAT_TIMEOUT_MS = 30000;//聊天接口超时
    private static final String FALLBACK_MSG = "抱歉，我暂时无法回答这个问题，建议拨打商家电话咨询。";//兜底

    /**
     * 项目启动完毕自动将文档向量存入redis
     */
    @PostConstruct
    public void buildIndex() {
        try {
            Set<String> existKeys = redisTemplate.keys(DOC_KEY_PREFIX + "*");
            if(existKeys != null && !existKeys.isEmpty()){
                log.info("RAG索引已存在，跳过启动构建");
                knowledgeBase = loadKnowledgeBase();
                return;
            }
            loadAndIndex();
            log.info("RAG index built successfully, {} docs", knowledgeBase.size());
        } catch (Exception e) {
            log.error("Failed to build RAG index", e);
        }
    }

    /**
     * ai回复
     * @param userId
     * @param question
     * @return
     */
    @Override
    public ChatResponseVO chat(Long userId, String question) {
        String historyKey = HISTORY_KEY_PREFIX + userId;

        // 1. 加载历史聊天
        List<ChatMessage> history = loadHistory(userId);
        // 2. 用户问题向量化
        float[] queryVec;
        try {
            queryVec = embed(question);
        } catch (Exception e) {
            log.error("Embedding failed for question: {}", question, e);
            return new ChatResponseVO(FALLBACK_MSG);
        }
        //检索相关文档对象
        List<KnowledgeDoc> retrieved = retrieve(queryVec, RETRIEVE_TOP_K);

        // 3.无匹配结果->存储记录
        if (retrieved.isEmpty()) {
            saveHistory(historyKey, history, question, FALLBACK_MSG);
            List<String> tips = new ArrayList<>();
            tips.add("推荐菜品");
            tips.add("配送时间");
            tips.add("如何退款");
            tips.add("联系商家");
            return new ChatResponseVO(FALLBACK_MSG, tips);
        }

        // 4. 构建提示词调用ai
        String prompt = buildPrompt(retrieved, history, question);
        String answer;
        try {
            answer = qianwenAPI.chat(prompt);
        } catch (Exception e) {
            log.error("Chat API failed, using retrieved docs as fallback", e);
            String fallback = retrieved.stream()
                    .findFirst()                    // 只取第一条（最相关）
                    .map(KnowledgeDoc::getContent)
                    .orElse(FALLBACK_MSG);
            // 添加前缀，让用户知道这是资料不是AI回复
            answer = "📚 根据资料显示：\n" + fallback;
        }

        // 5. 保存并返回
        saveHistory(historyKey, history, question, answer);
        return new ChatResponseVO(answer);
    }

    /**
     * 清空历史记录
     * @param userId
     */
    @Override
    public void clearHistory(Long userId) {
        redisTemplate.delete(HISTORY_KEY_PREFIX + userId);
    }

    /**
     * 将(content,向量)存入redis
     */
    private void loadAndIndex() {
        knowledgeBase = loadKnowledgeBase();//文档对象列表
        for (KnowledgeDoc doc : knowledgeBase) {
            try {
                float[] vec = embed(doc.getContent());//文档content转为向量
                DocVector dv = new DocVector(doc.getContent(), vec);//redis存储文档对象
                redisTemplate.opsForValue().set(DOC_KEY_PREFIX + doc.getId(), JSON.toJSONString(dv));
            } catch (Exception e) {
                log.error("Failed to embed doc {}: {}", doc.getId(), e.getMessage());
            }
        }
    }

    /**
     * 加载文档为对象列表
     * @return
     */
    private List<KnowledgeDoc> loadKnowledgeBase() {
        try {
            ClassPathResource resource = new ClassPathResource("knowledge-base.json");//resources下面的文件
            String json = new String(Files.readAllBytes(Paths.get(resource.getURI())), StandardCharsets.UTF_8);
            return JSON.parseArray(json, KnowledgeDoc.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load knowledge-base.json", e);
        }
    }

    /**
     * 通过http访问千问接口将文档对象转为向量
     * @param text
     * @return
     * @throws IOException
     */
    private float[] embed(String text) throws IOException {
        JSONObject body = new JSONObject();
        body.put("model", aiProperties.getEmbeddingModel());
        JSONObject input = new JSONObject();
        input.put("texts", Collections.singletonList(text));
        body.put("input", input);
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + aiProperties.getApiKey());
        headers.put("Content-Type", "application/json");

        String resp = HttpClientUtil.doPostJsonRaw(
                aiProperties.getEmbeddingEndpoint(), body.toJSONString(), headers, EMBED_TIMEOUT_MS);

        if (resp == null || resp.isEmpty()) {
            throw new IOException("Embedding API returned empty response");
        }
        JSONObject json = JSON.parseObject(resp);
        if (json == null) {
            throw new IOException("Failed to parse embedding API response");
        }
        JSONArray embeddings = json.getJSONObject("output").getJSONArray("embeddings");
        JSONArray arr = embeddings.getJSONObject(0).getJSONArray("embedding");
        float[] vec = new float[arr.size()];
        for (int i = 0; i < arr.size(); i++) vec[i] = arr.getFloatValue(i);
        return vec;
    }


    /**
     * 找匹配文档对象
     * @param queryVec
     * @param topK
     * @return
     */

    private List<KnowledgeDoc> retrieve(float[] queryVec, int topK) {
        //        redis找所有的文档向量对象
        Set<String> keys = redisTemplate.keys(DOC_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return Collections.emptyList();
        //         遍历得到匹配的文档的相似度分数
        List<DocScore> scores = new ArrayList<>();
        for (String key : keys) {
            String json = (String) redisTemplate.opsForValue().get(key);
            if (json == null) continue;
            DocVector dv  = JSON.parseObject(json, DocVector.class);
            double sim = cosineSimilarity(queryVec, dv.getVector());
            if (sim > SIM_THRESHOLD) {
                scores.add(new DocScore(key.replace(DOC_KEY_PREFIX, ""), sim));
            }
        }

        return scores.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .map(s -> knowledgeBase.stream()
                        .filter(d -> d.getId().equals(s.id))
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     *计算余弦相似度
     * @param a
     * @param b
     * @return
     */
    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 构建提示词(历史记录,匹配文档,问题)
     * @param retrieved
     * @param history
     * @param question
     * @return
     */
    private String buildPrompt(List<KnowledgeDoc> retrieved, List<ChatMessage> history, String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「苍穹外卖」的智能客服助手-小苍。请仅根据以下知识库内容回答用户问题。\n");
        sb.append("如果知识库中没有相关信息，回复：\"").append(FALLBACK_MSG).append("\"。\n");
        sb.append("请用友好、简洁的语言回答（不超过150字）：\n\n");

        sb.append("【知识库内容】\n");
        for (int i = 0; i < retrieved.size(); i++) {
            sb.append(i + 1).append(". ").append(retrieved.get(i).getContent()).append("\n");
        }

        if (!history.isEmpty()) {
            sb.append("\n【对话历史】\n");
            for (ChatMessage m : history) {
                sb.append(m.getRole().equals("user") ? "用户：" : "小苍：");
                sb.append(m.getContent()).append("\n");
            }
        }

        sb.append("\n【用户问题】\n").append(question);
        return sb.toString();
    }

    /**
     * 加载历史聊天,存在且有效则返回,否则返回null,过期则直接删掉
     * @return
     */
    @Override
    public List<ChatMessage> loadHistory(Long userId) {
        String key=HISTORY_KEY_PREFIX + userId;;
        String json = (String) redisTemplate.opsForValue().get(key);
        if (json == null) return new ArrayList<>();
        List<ChatMessage> res=JSON.parseArray(json, ChatMessage.class);
        if(isSessionExpired(res)){
            redisTemplate.delete(key);
            log.info("Session expired for user, resetting");
            return new ArrayList<>();
        }
        return res;
    }

    /**
     *保存对话到redis,仅仅保留最新的10轮对话并设置过期事件为2h
     * @param key
     * @param history
     * @param question
     * @param answer
     */

    private void saveHistory(String key, List<ChatMessage> history, String question, String answer) {
        history.add(new ChatMessage("user", question));
        history.add(new ChatMessage("assistant", answer));
        int max = aiProperties.getMaxHistory() * 2;
        if (history.size() > max) history = history.subList(history.size() - max, history.size());
        redisTemplate.opsForValue().set(key, JSON.toJSONString(history),
                aiProperties.getTtl(), TimeUnit.SECONDS);
    }

    /**
     * 检验历史对话是否过期
     * @param history
     * @return
     */
    private boolean isSessionExpired(List<ChatMessage> history) {
        if (history.isEmpty()) return false;
        long lastTime = history.get(history.size() - 1).getTimestamp();
        return (System.currentTimeMillis() - lastTime) > aiProperties.getTtl() * 1000L;
    }

    // =========== Inner class ===========

    @AllArgsConstructor
    private static class DocScore {
        String id;
        double score;
    }
}
