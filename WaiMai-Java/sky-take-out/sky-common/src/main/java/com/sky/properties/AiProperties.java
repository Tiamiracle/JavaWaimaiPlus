package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "qianwen")
@Data
public class AiProperties {
    private String embeddingEndpoint;//向量接口
    private String embeddingModel;//向量模型
    private String chatEndpoint;//聊天接口
    private String chatModel;//大模型模型
    private String apiKey;//apikey
    private int maxHistory;//最大历史消息条数方式prompt过长
    private int ttl;//redis存储事件2h
}
