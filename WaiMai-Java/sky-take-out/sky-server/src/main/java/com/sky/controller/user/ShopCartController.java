package com.sky.controller.user;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShopCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
public class ShopCartController {
    @Autowired
    private ShopCartService shopCartService;
    /**
     * 添加购物车dishid/setmealid/flavor
     */
    @PostMapping ("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车：{}", shoppingCartDTO);
        shopCartService.add(shoppingCartDTO);
        return Result.success();
    }
    /**
     * 查看购物车
     */
    @GetMapping("/list")
    public Result get() {
        List<ShoppingCart> list=shopCartService.list();
        return Result.success(list);
    }

    /**
     * 减少购物车数量
     * @return
     */
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shopCartService.sub(shoppingCartDTO);
        return Result.success();
    }
    @DeleteMapping("/clean")
    public Result clean() {
        shopCartService.clean();
        return Result.success();
    }
}
