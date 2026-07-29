package com.sky.mapper;

import com.sky.aspect.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId} order by id")
    List<SetmealDish> listBySetmealId(Long setmealId);

    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    void addBatch(List<SetmealDish> setmealDishes);
    @Select("select setmeal_id from setmeal_dish where dish_id = #{id}")
    List<Long> getSetmealIdsByDishId(Long id);

}
