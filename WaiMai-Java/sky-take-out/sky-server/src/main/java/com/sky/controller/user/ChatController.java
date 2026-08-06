package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ChatSendDTO;
import com.sky.rag.ChatMessage;
import com.sky.result.Result;
import com.sky.service.ChatService;
import com.sky.vo.ChatResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/user/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;
    @GetMapping("/history")
    public Result<List<ChatMessage>> loadHistory() {
        Long userId = getCurrentUserId();
        List<ChatMessage> history = chatService.loadHistory(userId);
        return Result.success(history);
    }

    @PostMapping("/send")
    @ApiOperation("发送消息")
    public Result<ChatResponseVO> send(@RequestBody ChatSendDTO dto) {
        log.info("====chat接口被调用了===，参数：{}",dto);
        Long userId = getCurrentUserId();
        ChatResponseVO vo = chatService.chat(userId, dto.getContent());
        return Result.success(vo);
    }

    @DeleteMapping("/history")
    @ApiOperation("清空会话")
    public Result clearHistory() {
        chatService.clearHistory(getCurrentUserId());
        return Result.success();
    }

    private Long getCurrentUserId() {
        return  BaseContext.getCurrentId();
    }
}
