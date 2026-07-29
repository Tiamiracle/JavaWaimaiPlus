package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
public class UserLoginDTO implements Serializable {
    private String code;           // 微信登录 code
    private String phoneCode;      // 手机号授权 code
    private String encryptedData;  // 加密数据（用于解密手机号）
    private String iv;             // 初始向量

}
