package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO getById(Long id);

    void add(SetmealDTO setmealDTO);

    void update(SetmealDTO setmealDTO);

    void delete(String ids);

    void updateStatus(Integer status, Long id);

    List<SetmealVO> list(Long categoryId);
}
