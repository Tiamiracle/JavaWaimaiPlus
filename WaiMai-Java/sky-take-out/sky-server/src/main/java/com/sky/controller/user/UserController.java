package com.sky.controller.user;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.DishVO;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
//    微信登录接口
    @PostMapping("/user/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        return Result.success(userService.login(userLoginDTO));
    }
//    获取店铺联系方式
    @GetMapping("/shop/getMerchantInfo")
    public Result getMerchantInfo(){
        return Result.success(userService.getMerchantInfo());
    }
//    通过id获取用户信息
    @GetMapping("/user/{id}")
    public Result<User> getUserById(@PathVariable Long id){
        return Result.success(userService.getUserById(id));
    }
//    通过id更改用户信息
    @PutMapping("/user/{id}")
    public Result updateUserById(@PathVariable Long id, @RequestBody User user){
        userService.updateUserById(id, user);
        return Result.success();
    }
}
