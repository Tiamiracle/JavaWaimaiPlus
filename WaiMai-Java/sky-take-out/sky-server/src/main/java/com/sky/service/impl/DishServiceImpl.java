package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.exception.BaseException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.utils.QianwenAPI;
import com.sky.vo.DishVO;
import com.sky.vo.RecommendDishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private QianwenAPI qianwenAPI;
    /**
     * PageResult->total+records
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> result=dishMapper.page(dishPageQueryDTO);
        return new PageResult(result.getTotal(),result.getResult());
    }

    /**
     * 新增菜品
     * 1.菜品必须要属于分类
     * 2.菜品的默认状态为启售
     * 3.菜品name唯一
     * 4.有口味则菜品添加涉及到对应口味添加（事务）
     * @param dishDTO
     */
    @Override
    @Transactional
    public void add(DishDTO dishDTO) {
        if(dishMapper.getByName(dishDTO.getName())!=null){
            throw new BaseException("菜品已存在");
        }
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(0);
        dishMapper.add(dish);
        if(dishDTO.getFlavors()!=null&&dishDTO.getFlavors().size()>0){
            dishDTO.getFlavors().forEach(dishFlavor -> dishFlavor.setDishId(dish.getId()));
//            有口味则添加
            dishFlavorMapper.addBatch(dishDTO.getFlavors());
        }
    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        Dish dish=dishMapper.getById(dishDTO.getId());
        String name=dishDTO.getName();
        if(dishDTO.getName()!=null&&!name.equals(dish.getName())&&dishMapper.getByName(name)!=null){
            throw new BaseException("菜品已存在");
        }
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        if(dishDTO.getFlavors()!=null&&dishDTO.getFlavors().size()>0){
//            更新口味
            dishFlavorMapper.deleteByDishId(dish.getId());
            dishDTO.getFlavors().forEach(dishFlavor -> dishFlavor.setDishId(dish.getId()));
            dishFlavorMapper.addBatch(dishDTO.getFlavors());
        }
    }

    /**
     * 根据id查询菜品+口味信息
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        Dish dish=dishMapper.getById(id);
        DishVO dishVo=new DishVO();
        BeanUtils.copyProperties(dish,dishVo);
        dishVo.setFlavors(dishFlavors);
        return dishVo;
    }
    /**
     * 对于菜品的启用和禁用，启用可以直接，禁用则提示，删除则禁用就可以删除
     * ,分割各个id
     */
    @Override
    @Transactional
    public void delete(String ids) {
        String[] split = ids.split(",");
        for (String id : split) {
            Dish dish=dishMapper.getById(Long.valueOf(id));
            if (dish.getStatus() == 1)
                throw new BaseException(dishMapper.getById(Long.valueOf(id)).getName()+MessageConstant.DISH_ON_SALE);
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(Long.valueOf(id));
            if (setmealIds != null && setmealIds.size() > 0) {
                throw new BaseException(dishMapper.getById(Long.valueOf(id)).getName()+MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            dishMapper.delete(Long.valueOf(id));
        }
    }

    /**
     * 获取当前用户的推荐菜品
     * @param userId
     * @param limit
     * @return
     */
    @Override
    public List<RecommendDishVO> recommend(Long userId, Integer limit) {
//        根据用户id查询当前用户的订单
        List<Orders> orders = orderMapper.getByUserId(Orders.COMPLETED,userId);
        List<RecommendDishVO> res = null;
        if(orders!=null&&orders.size()!=0){
            //        用户有订单，协同过滤获取推荐菜品
            res = dishMapper.recommendByCF(Orders.COMPLETED,userId, limit);

        }else{
            //        用户没有订单，返回销售量最多的toplimit为推荐菜品
            res = dishMapper.recommendBySales(Orders.COMPLETED,userId,limit);
        }
        if(res == null || res.isEmpty()){
//            冷处理兜底
            res = dishMapper.recommendBySales(Orders.COMPLETED,userId,limit);

        }
        if (res != null && !res.isEmpty()) {
            for (RecommendDishVO dish : res) {
                List<DishFlavor> flavors = dishFlavorMapper.getByDishId(dish.getId());
                dish.setFlavors(flavors);
                //        生成推荐理由
                String reason = generateReason(dish.getName());
                dish.setReason(reason);
            }
        }
        return res;
    }
    private String generateReason(String dishName) {
        try {
            String reason = qianwenAPI.generateRecommendReason(dishName);
            if (reason != null && !reason.isEmpty()) {
                return reason;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "🔥 为你推荐";
    }

    /**
     *
     * 更改状态
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        if (status == 1) {
            Long categoryId=dishMapper.getById(id).getCategoryId();
            if(categoryMapper.getById(categoryId).getStatus()==0)
                throw new BaseException("当前分类已禁用，无法启用菜品");
            dishMapper.update(dish);
        } else {
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(id);
            if (setmealIds != null && setmealIds.size() > 0) {
                setmealIds.forEach(setmealId -> {
                    if(setmealMapper.getById(setmealId).getStatus()==1){
                        throw new BaseException(setmealMapper.getById(setmealId).getName()+MessageConstant.SETMEAL_ON_SALE);
                    }
                });
            }
            dishMapper.update(dish);
        }
    }

    @Override
    public List<DishVO> list(Long categoryId) {
        List<DishVO> dishList=dishMapper.list(categoryId);
        dishList.forEach(dishVo -> {
            List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(dishVo.getId());
            dishVo.setFlavors(dishFlavors);
        });
        return dishList;
    }


}
