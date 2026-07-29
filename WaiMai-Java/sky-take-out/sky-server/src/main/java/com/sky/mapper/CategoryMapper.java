package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.aspect.AutoFill;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {
    Page<Category> page(CategoryPageQueryDTO pageQueryDTO);
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into category(type, name, sort, status, create_user, update_user) values(#{type}, #{name}, #{sort}, #{status},#{createUser}, #{updateUser})")
    void add(Category category);
    @Select("select * from category where name = #{name}")
    Category getByName(String name);
    @Select("select * from category where id = #{id}")
    Category getById(Long id);
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);
    @Select("select * from category where type = #{type}")
    List<Category> list(Integer type);
    @Delete("delete from category where id = #{id}")
    void delete(Long id);
}
