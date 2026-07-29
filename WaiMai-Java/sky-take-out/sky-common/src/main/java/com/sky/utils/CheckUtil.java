package com.sky.utils;

import com.sky.exception.BaseException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
//校验工具类
public class CheckUtil {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
//    字符串校验
    public static boolean checkStr(String str) {
        if (str == null || str.isEmpty()) {
            throw new BaseException("字段不能为空");
        }
        return true;
    }
//    电话校验
    public static boolean checkPhone(String phone){
        if (!checkStr(phone)) {
           throw new BaseException("电话号码不能为空");
        }
        // 去除首尾空格再匹配正则
        if (! PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BaseException("电话号码格式不正确");
        }
        return true;
    }

//    邮箱校验
    public static boolean checkEmail(String email) {
       if (!checkStr(email)) {
            throw new BaseException("邮箱不能为空");
       }
        // 去除首尾空格再匹配正则
        if (! EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BaseException("邮箱格式不正确");
        }
        return true;
    }
}
