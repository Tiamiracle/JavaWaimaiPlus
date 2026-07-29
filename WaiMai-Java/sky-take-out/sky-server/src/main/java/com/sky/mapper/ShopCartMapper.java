package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShopCartMapper {
    //    更新购物车数量
    @Update("UPDATE shopping_cart SET number = #{number} WHERE id = #{id}")
    void updateNumber(ShoppingCart shoppingCart);
    //    添加购物车
    @Insert("INSERT INTO shopping_cart ( name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount) VALUES (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount})")
    void insert(ShoppingCart shoppingCart);

    //    商品是否存在
    ShoppingCart isExist(ShoppingCart shoppingCart);
    //       查询购物车列表
    @Select("SELECT * FROM shopping_cart WHERE user_id = #{userId}")
    List<ShoppingCart> list(Long userId);
    @Delete("DELETE FROM shopping_cart WHERE id = #{id}")
    void delete(ShoppingCart shoppingCart);
    @Delete("DELETE FROM shopping_cart WHERE user_id = #{userId}")
    void clean(Long userId);
}
