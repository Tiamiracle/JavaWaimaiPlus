package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import com.sky.vo.RecommendDishVO;

import java.util.List;

public interface DishService {
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void add(DishDTO dishDTO);

    void update(DishDTO dishDTO);

    DishVO getById(Long id);

    void updateStatus(Integer status, Long id);

    List<DishVO> list(Long categoryId);

    void delete(String ids);

    List<RecommendDishVO> recommend(Long userId, Integer limit);
}
