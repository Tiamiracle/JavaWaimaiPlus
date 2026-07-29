package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.Category;
import com.sky.entity.User;
import com.sky.vo.DishVO;
import com.sky.vo.UserLoginVO;

import java.util.List;

public interface UserService {
    UserLoginVO login(UserLoginDTO userLoginDTO);

    String getMerchantInfo();

    User getUserById(Long id);

    void updateUserById(Long id, User user);
}
