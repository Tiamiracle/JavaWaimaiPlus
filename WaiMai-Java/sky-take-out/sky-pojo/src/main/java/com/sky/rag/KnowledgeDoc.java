package com.sky.rag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 知识库文档对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDoc {
    private String id;
    private String type;
    private String title;
    private String content;
    private List<String> tags;
}
