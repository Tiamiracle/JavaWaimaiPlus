package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        if (setmeal == null) {
            throw new BaseException("套餐不存在");
        }
        SetmealVO vo = new SetmealVO();
        BeanUtils.copyProperties(setmeal, vo);
        Category category = categoryMapper.getById(setmeal.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        List<SetmealDish> dishes = setmealDishMapper.listBySetmealId(id);
        if (dishes != null) {
            vo.setSetmealDishes(dishes);
        }
        return vo;
    }

    /**
     * 新增套餐
     * @param setmealDTO
     * 添加套餐
     * 添加套餐对应菜品,默认套餐为禁售
     *
     */
    @Override
    @Transactional
    public void add(SetmealDTO setmealDTO) {
        String setmealName = setmealDTO.getName();
        if (setmealMapper.countByName(setmealName) > 0) {
            throw new BaseException("该套餐名称已存在，请重新命名");
        }
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(0);
        setmealMapper.add(setmeal);
        Long id = setmeal.getId();
        if(setmealDTO.getSetmealDishes()!=null&&setmealDTO.getSetmealDishes().size()>0){
            setmealDTO.getSetmealDishes().forEach(dish -> dish.setSetmealId(id));
            setmealDishMapper.addBatch(setmealDTO.getSetmealDishes());
        }
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        if (setmealDTO.getId() == null) {
            throw new BaseException("套餐id不能为空");
        }
        Setmeal existed = setmealMapper.getById(setmealDTO.getId());
        if (existed == null) {
            throw new BaseException("套餐不存在");
        }
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        if (setmealDTO.getSetmealDishes() != null && setmealDTO.getSetmealDishes().size() > 0) {
            setmealDTO.getSetmealDishes().forEach(dish -> dish.setSetmealId(setmealDTO.getId()));
            setmealDishMapper.addBatch(setmealDTO.getSetmealDishes());
        }
    }


    @Override
    @Transactional
    public void delete(String ids) {
        String[] idArray = ids.split(",");
        for(String id : idArray){
            Setmeal setmeal = setmealMapper.getById(Long.valueOf(id));
            if(setmeal.getStatus()==1)
                throw new BaseException(setmeal.getName() + MessageConstant.SETMEAL_ON_SALE);
            setmealDishMapper.deleteBySetmealId(Long.valueOf(id));
            setmealMapper.delete(Long.valueOf(id));
        }
    }

    /**
     * 套餐更改状态：启用-》分类为禁售则不能启售、分类下的菜品都是启售才可以启售，禁售-》可以直接，删除-》当前为启售则不能删除
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, Long id) {
       Long categoryId= setmealMapper.getById(id).getCategoryId();
        if (status == 1) {
            if(categoryMapper.getById(categoryId).getStatus()==0)
                throw new BaseException("当前分类为禁售，无法启售套餐");
            setmealDishMapper.listBySetmealId(id).forEach(dish -> {
                if (dishMapper.getById(dish.getDishId()).getStatus() == 0) {
                    throw new BaseException(dishMapper.getById(dish.getDishId()).getName()+MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }
    @Override
    public List<SetmealVO> list(Long categoryId) {
        List<SetmealVO> setmealVOs = setmealMapper.getByCategoryId(categoryId);
        // 2. 遍历每个套餐，查询其菜品详情
        for (SetmealVO setmealVO : setmealVOs) {
            List<SetmealDish> dishes = setmealDishMapper.listBySetmealId(setmealVO.getId());
            setmealVO.setSetmealDishes(dishes);  // 单独设置
        }
        return setmealVOs;
    }
}
