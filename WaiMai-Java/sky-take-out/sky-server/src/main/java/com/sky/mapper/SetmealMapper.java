package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.aspect.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SetmealMapper {

    Page<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO);

    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    @AutoFill(value = OperationType.INSERT)
    void add(Setmeal setmeal);

    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);
    @Update("update setmeal set status = #{status}, update_user = #{updateUser} where category_id = #{categoryId}")
    void updateStatusByCategoryId(@Param("status") Integer status, @Param("categoryId") Long categoryId, @Param("updateUser") Long updateUser);

    List<SetmealVO> getByCategoryId(Long categoryId);

    @Delete("delete from setmeal where id = #{id}")
    void delete(Long id);

    @Select("select count(*) from setmeal where name = #{setmealName}")
    int countByName(String setmealName);
    @Select("select count(*) from setmeal where status = #{status}")
    Long countByStatus(Integer status);
}
