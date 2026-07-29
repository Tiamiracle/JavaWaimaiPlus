package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;

import java.util.List;

public interface CategoryService {
    PageResult page(CategoryPageQueryDTO pageQueryDTO);

    void add(CategoryDTO categoryDTO);

    void update(CategoryDTO categoryDTO);

    void changeStatus(Integer status, Long id);

    void delete(Long id);

    List<Category> list(Integer type);
}
