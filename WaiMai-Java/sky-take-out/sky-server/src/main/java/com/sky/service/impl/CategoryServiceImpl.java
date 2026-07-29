package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public PageResult page(CategoryPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<Category> result= categoryMapper.page(pageQueryDTO);
        PageResult pageResult = new PageResult(result.getTotal(), result.getResult());
        return pageResult;
    }

    @Override
    public void add(CategoryDTO categoryDTO) {
        if(categoryMapper.getByName(categoryDTO.getName())!=null)
            throw new BaseException(MessageConstant.CATEGORY_DUPLICATED);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setStatus(0);
        categoryMapper.add(category);
    }

    /**
     * 修改分类
     * id名称改变了并且这个新名称已经存在-》异常
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category=categoryMapper.getById(categoryDTO.getId());
        if(!(categoryDTO.getName().equals(category.getName()))&&categoryMapper.getByName(categoryDTO.getName())!=null)
            throw new BaseException(MessageConstant.CATEGORY_DUPLICATED);
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.update(category);
    }

    /**
     * 启用可以直接启用，但是禁用需要将分类下的菜品或套餐都禁用。删除则需要分类下没有菜品或套餐才可以删除。
     */
    @Override
    @Transactional
    public void changeStatus(Integer status, Long id) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryMapper.update(category);
        if (status == 0) {
            Long updateUser = BaseContext.getCurrentId();
            setmealMapper.updateStatusByCategoryId(status, id, updateUser);
            dishMapper.updateStatusByCategoryId(status, id, updateUser);
        }
    }

    /**
     * 删除分类
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        if(dishMapper.getByCategoryId(id)!= null&&dishMapper.getByCategoryId(id).size()>0)
            throw new BaseException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        else if(categoryMapper.getById(id).getStatus()==1)
            throw new BaseException("启售中的分类不能删除");
        else if(setmealMapper.getByCategoryId(id)!=null&&setmealMapper.getByCategoryId(id).size()>0)
            throw new BaseException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        else{
            categoryMapper.delete(id);
        }
    }

    @Override
    public List<Category> list(Integer type) {
        List<Category> result= categoryMapper.list(type);
        return result;
    }
}
