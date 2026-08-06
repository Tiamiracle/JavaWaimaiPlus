package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseVO implements Serializable {
    private String content;// 机器人回复的内容
    private List<String> suggestions;//快捷问题列表

    public ChatResponseVO(String content) {
        this.content = content;
    }
}
