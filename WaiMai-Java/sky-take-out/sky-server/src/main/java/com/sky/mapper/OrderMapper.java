package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrderCountDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.TopCountDTO;
import com.sky.dto.UserCountDTO;
import com.sky.entity.Category;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);
    @Select("select * from orders where number = #{orderNumber} and user_id = #{userId}")
    Orders getByNumber(String orderNumber,Long userId);
    @Select("select * from orders where status =#{status} and user_id = #{userId}")
    List<Orders> getByUserId(Integer status,Long userId);
    void update(Orders orders);
    Page<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);
    @Select("select * from orders where id = #{id}")
    Orders getByOrderId(Long id);
    @Select("select * from orders where status = #{status}")
    List<Orders> listByStatus(@Param("status") Integer status);
    Page<Orders> userPage(OrdersPageQueryDTO ordersPageQueryDTO, Long userId);
    @Select("select * from orders where status = #{status} and order_time<#{timeThreshold}")
    List<Orders> getTimeoutUnpaidOrders(@Param("status")Integer status, @Param("timeThreshold")LocalDateTime timeThreshold);
    Long countByStatus(Integer status,LocalDate date);
    Double getTurnoverStatistics(LocalDate begin,LocalDate end);
    OrderCountDTO getOrderList(LocalDate begin,LocalDate end);
    List<TopCountDTO>getTop10(LocalDate beginDate,LocalDate endDate);
}
