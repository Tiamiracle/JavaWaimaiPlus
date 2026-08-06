package com.sky.service;

import com.sky.rag.ChatMessage;
import com.sky.vo.ChatResponseVO;

import java.util.List;

public interface ChatService {
    List<ChatMessage> loadHistory(Long userId);
    ChatResponseVO chat(Long userId, String question);
    void clearHistory(Long userId);
}
