package com.sky.utils;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Arrays;

//千文工具api生成推荐理由
@Component
public class QianwenAPI {
    @Value("${qianwen.api-key}")
    private String apiKey;
    @Value("${qianwen.model.chat}")
    private String model;
    private Generation generation;

    @PostConstruct
    public void init() {
       generation = new Generation();
    }

    /**
     * 生成推荐理由
     */
    public String generateRecommendReason(String dishName) {
        try {
            String prompt = String.format(
                    "推荐菜品：%s。用一句15字以内的话推荐，语气亲切，不用解释原因。",
                    dishName
            );
            return chat(prompt);
        } catch (Exception e) {
            return "🔥 为你推荐";  // 兜底
        }
    }

    /**
     * 通用对话
     */
    public String chat(String prompt) {
        try {
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt)
                    .build();

            GenerationParam param = GenerationParam.builder()
                    .model(model)
                    .apiKey(apiKey)
                    .messages(Arrays.asList(userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            GenerationResult result = generation.call(param);
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            return null;
        }
    }
}
