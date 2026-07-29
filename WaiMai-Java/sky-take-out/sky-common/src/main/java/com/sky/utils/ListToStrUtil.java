package com.sky.utils;

import java.util.List;

public class ListToStrUtil {
    // 拼接任意对象List为逗号分隔字符串
    public static <T> String join(List<T> list) {
        if(list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (T t : list) {
            if(sb.length() > 0) sb.append(",");
            sb.append(t);
        }
        return sb.toString();
    }
}
