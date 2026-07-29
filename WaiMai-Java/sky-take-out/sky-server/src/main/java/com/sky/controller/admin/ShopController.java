package com.sky.controller.admin;

import com.sky.config.RedisConfiguration;
import com.sky.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
public class ShopController {
    @Autowired
    private RedisTemplate  redisTemplate;
/**
 * 设置店铺状态
 * PUT/admin/shop/{status}
 */
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable("status") Integer status) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("shop_status", status);
        return Result.success();
    }
//    查看店铺状态
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer status= (Integer) valueOperations.get("shop_status");
        return Result.success(status);
    }
}
