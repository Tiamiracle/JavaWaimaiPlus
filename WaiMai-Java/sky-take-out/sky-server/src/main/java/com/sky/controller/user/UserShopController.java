package com.sky.controller.user;

import com.sky.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/shop")
public class UserShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    //    查看店铺状态
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer status= (Integer) valueOperations.get("shop_status");
        return Result.success(status);
    }
}
