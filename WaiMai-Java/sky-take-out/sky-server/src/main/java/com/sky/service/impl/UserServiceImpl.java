package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.entity.User;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.DishVO;
import com.sky.vo.UserLoginVO;
import java.util.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private CategoryMapper  categoryMapper;
    private String cachedAccessToken;    // 缓存的 token
    private long expireTime;             // 过期时间戳（毫秒）


    /**
     * 获取 access_token
     */
    private String getAccessToken() {
        // 缓存有效，直接返回
        if (cachedAccessToken != null && System.currentTimeMillis() < expireTime) {
            return cachedAccessToken;
        }
        return refreshAccessToken();
    }

    /**
     * 刷新 access_token
     * synchronized线程锁，当多个用户进行授权登录发现accesstoken过期了，只需要refresh一次就好了
     */
    private synchronized String refreshAccessToken() {
        // 双重检查：可能其他线程已经刷新了
        if (cachedAccessToken != null && System.currentTimeMillis() < expireTime) {
            return cachedAccessToken;
        }

        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=" + weChatProperties.getAppid()
                + "&secret=" + weChatProperties.getSecret();

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String body = EntityUtils.toString(response.getEntity());
            httpClient.close();
            response.close();

            JSONObject json = JSONObject.parseObject(body);

            // 检查是否有错误
            if (json.containsKey("errcode")) {
                String errMsg = json.getString("errmsg");
                throw new BaseException("获取 access_token 失败: " + errMsg);
            }

            String accessToken = json.getString("access_token");
            Integer expiresIn = json.getInteger("expires_in");  // 默认 7200 秒

            if (accessToken == null || accessToken.isEmpty()) {
                throw new BaseException("获取 access_token 失败: access_token 为空");
            }

            // 缓存 access_token，提前 5 分钟过期（防止刚好过期）
            cachedAccessToken = accessToken;
            expireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000;
            return accessToken;

        } catch (Exception e) {
            throw new BaseException("获取 access_token 失败: " + e.getMessage());
        }
    }
    private String getPhoneNumberByCode(String phoneCode, String accessToken) {
        String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("code", phoneCode);
            httpPost.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));

            CloseableHttpResponse response = httpClient.execute(httpPost);
            String body = EntityUtils.toString(response.getEntity());
            httpClient.close();
            response.close();

            JSONObject json = JSONObject.parseObject(body);

            // 检查返回结果
            Integer errcode = json.getInteger("errcode");
            if (errcode != null && errcode != 0) {
                String errMsg = json.getString("errmsg");
                throw new BaseException("获取手机号失败: " + errMsg);
            }

            JSONObject phoneInfo = json.getJSONObject("phone_info");
            if (phoneInfo == null) {
                throw new BaseException("获取手机号失败: 返回数据为空");
            }

            String phoneNumber = phoneInfo.getString("phoneNumber");

            return phoneNumber;

        } catch (Exception e) {
            throw new BaseException("获取手机号失败: " + e.getMessage());
        }
    }
    /**
     * 解密手机号
     */
    public String decryptPhoneNumber(String encryptedData, String sessionKey, String iv) {
        try {
            // 1. Base64 解码
            byte[] dataByte = Base64.getDecoder().decode(encryptedData);
            byte[] keyByte = Base64.getDecoder().decode(sessionKey);
            byte[] ivByte = Base64.getDecoder().decode(iv);

            // 2. AES 解密
            SecretKeySpec keySpec = new SecretKeySpec(keyByte, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivByte));
            byte[] resultByte = cipher.doFinal(dataByte);

            // 3. 转成 JSON 字符串
            String result = new String(resultByte);
            JSONObject json = JSONObject.parseObject(result);

            // 4. 提取手机号
            return json.getString("phoneNumber");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String code=userLoginDTO.getCode();
        String phoneCode=userLoginDTO.getPhoneCode();
        String encryptedData=userLoginDTO.getEncryptedData();
        String iv=userLoginDTO.getIv();

        if (code == null || code.trim().isEmpty()) {
            throw new BaseException("获取用户信息错误");
        }
//        微信登录
        String url="https://api.weixin.qq.com/sns/jscode2session?appid="+weChatProperties.getAppid()+"&secret="+weChatProperties.getSecret()+"&js_code="+code+"&grant_type=authorization_code";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet=new HttpGet(url);
            CloseableHttpResponse res = httpClient.execute(httpGet);
            String body = EntityUtils.toString(res.getEntity());
            JSONObject json = JSONObject.parseObject(body);
            if(json.containsKey("errcode")){
                String msg = json.getString("errmsg");
                throw new BaseException("授权失效，请重新点击登录："+msg);
            }
            String openid = json.getString("openid");
            String sessionKey=json.getString("session_key");//session_key
            if(openid == null)
                throw new BaseException("微信登录失败");
            User user = userMapper.getByOpenid(openid);
            Long userId;
            if(user == null){
                //            解密手机号
                String phoneNumber = null;
                if (encryptedData != null && !encryptedData.isEmpty()
                        && iv != null && !iv.isEmpty() && sessionKey != null) {
                    phoneNumber = decryptPhoneNumber(encryptedData, sessionKey, iv);
                }
                else if (phoneCode != null && !phoneCode.isEmpty()) {
                    String accessToken = getAccessToken();  // 需要实现这个方法
                    phoneNumber = getPhoneNumberByCode(phoneCode, accessToken);
                }
                if (phoneNumber == null || phoneNumber.isEmpty()) {
                    throw new BaseException("获取手机号失败，请重新授权");
                }
//                初始化新用户默认数据
                String str="https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132";
                String name="微信用户";
                String sex="0";
//                添加新用户
                user = User.builder().avatar(str).name(name).sex(sex).phone(phoneNumber).openid(openid).build();
                userMapper.insert(user);
            }
//          旧用户
            userId = user.getId();
//           设置token
            Map<String, Object> map = new HashMap<>();
            map.put(JwtClaimsConstant.USER_ID, userId);
            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(), map);
            httpClient.close();
            res.close();
            return UserLoginVO.builder().id(userId).openid(openid).token(token).deliveryFee(BigDecimal.TEN).shopName("苍穹外卖").shopAddress("陕西省西安市").build();
        } catch (Exception e) {
            throw new BaseException("微信登录失败"+e.getMessage());
        }
    }

    @Override
    public String getMerchantInfo() {
        Employee user=employeeMapper.getById(1L);
        return user.getPhone();
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.getById(id);
    }

    @Override
    public void updateUserById(Long id, User user) {
        User user1=userMapper.getById(id);
        if(user1==null)
            throw new BaseException("用户不存在");
        user1.setName(user.getName());
        user1.setSex(user.getSex());
        user1.setIdNumber(user.getIdNumber());
        user1.setAvatar(user.getAvatar());
        userMapper.update(user1);
    }

}
