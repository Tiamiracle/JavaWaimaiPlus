package com.sky.rag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *文档+向量（Redis存储）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocVector {
    private String text;
    private float[] vector;
}
