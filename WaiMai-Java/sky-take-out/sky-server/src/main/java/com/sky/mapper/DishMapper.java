package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.aspect.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import com.sky.vo.RecommendDishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper {
    /**
     * 协同过滤推荐（基于用户历史订单）
     */
    List<RecommendDishVO> recommendByCF(@Param("status")Integer status,@Param("userId") Long userId,
                                        @Param("limit") Integer limit);

    /**
     * 销量排行兜底（冷启动）
     */
    List<RecommendDishVO> recommendBySales(@Param("status")Integer status,Long userId, @Param("limit") Integer limit);
    Page<DishVO> page(DishPageQueryDTO dishPageQueryDTO);

    @Select("SELECT * FROM dish WHERE name = #{name}")
    Dish getByName(String name);

    @AutoFill(value = OperationType.INSERT)
    void add(Dish dish);

    @Select("SELECT * FROM dish WHERE id = #{id}")
    Dish getById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    List<DishVO> list(Long categoryId);

    @Update("UPDATE dish SET status = #{status}, update_user = #{updateUser} WHERE category_id = #{categoryId}")
    void updateStatusByCategoryId(@Param("status") Integer status, @Param("categoryId") Long categoryId, @Param("updateUser") Long updateUser);

    @Select(" select d.*,c.name as categoryname from dish d LEFT JOIN  category c on d.category_id=c.id where d.category_id = #{categoryId}")
    List<DishVO> getByCategoryId(Long categoryId);

    @Delete("DELETE FROM dish WHERE id = #{id}")
    void delete(Long valueOf);

    @Select("SELECT count(*) FROM dish WHERE status = #{status}")
    Long countByStatus(Integer status);
}
