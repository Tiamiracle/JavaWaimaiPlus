package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShopCartMapper;
import com.sky.service.ShopCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopCartServiceImpl implements ShopCartService {
    @Autowired
    private ShopCartMapper shopCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车,1.通过userId和dishId+flavor/setmealId看购物车表格有没有数据，2.有就直接数量加一3.没有就添加数据
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        Long userId=BaseContext.getCurrentId();
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(userId);
        if(shoppingCart.getDishId()!=null){
//            加入的是菜品
            Dish dish=dishMapper.getById(shoppingCart.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        }
        else{
            //加入的是套餐
            Setmeal setmeal=setmealMapper.getById(shoppingCart.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }
        shoppingCart.setNumber(1);
        ShoppingCart cart=shopCartMapper.isExist(shoppingCart);
        if(cart!=null){
            //存在，更新购物车数量
            shoppingCart.setId(cart.getId());
            shoppingCart.setNumber(cart.getNumber()+1);
            shopCartMapper.updateNumber(shoppingCart);
        }else{
            //不存在，添加购物车数据
            shopCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Long userId=BaseContext.getCurrentId();
        return shopCartMapper.list(userId);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        Long userId=BaseContext.getCurrentId();
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(userId);
        ShoppingCart cart=shopCartMapper.isExist(shoppingCart);
        cart.setNumber(cart.getNumber()-1);
        if(cart.getNumber()>0){
            shopCartMapper.updateNumber(cart);
        }else{
//            删除购物车数据
            shopCartMapper.delete(cart);
        }
    }

    /**
     * 清空购物车
     */

    @Override
    public void clean() {
        Long userId=BaseContext.getCurrentId();
        shopCartMapper.clean(userId);
    }
}
