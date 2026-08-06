package com.sky.rag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * redis存储聊天对象
 * role->user 用户消息；assistant AI 机器人消息
 * content->消息内容
 * timestamp->事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String content;
    private long timestamp;

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
}
