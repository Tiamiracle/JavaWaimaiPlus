package com.sky.service.impl;

import com.sky.dto.OrderCountDTO;
import com.sky.dto.OrdersDTO;
import com.sky.dto.UserCountDTO;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public SetmealOverViewVO overviewSetmeals() {
        Long saleCount=setmealMapper.countByStatus(1);
        Long stopCount=setmealMapper.countByStatus(0);
        return SetmealOverViewVO.builder().sold(saleCount).discontinued(stopCount).build();
    }
//今日商业数据
    @Override
    public BusinessDataVO businessData(LocalDate begin,LocalDate end) {
        Double turnover=orderMapper.getTurnoverStatistics(begin,end);
        OrderCountDTO orderCountDTO=orderMapper.getOrderList(begin,end);
        Long validOrderCount=orderCountDTO.getValidOrderCount();
        Long allOrders=orderCountDTO.getOrderCount();
        Double orderCompletionRate=allOrders==0?0:(double)validOrderCount/allOrders;
        Double unitPrice=validOrderCount==0?0:turnover/validOrderCount;
        UserCountDTO userCountDTO=userMapper.getUserList(begin,end);
        Long dayNewUser=userCountDTO.getDayNewUser();
        return BusinessDataVO.builder().turnover(turnover).validOrderCount(validOrderCount).orderCompletionRate(orderCompletionRate).unitPrice(unitPrice).newUsers(dayNewUser).build();
    }
    @Override
    public DishOverViewVO overviewDishes() {
        Long saleCount=dishMapper.countByStatus(1);
        Long stopCount=dishMapper.countByStatus(0);
        return DishOverViewVO.builder().sold(saleCount).discontinued(stopCount).build();
    }
//    今日订单数据

    @Override
    public OrderOverViewVO overviewOrders(LocalDate begin, LocalDate end) {
        LocalDate now = LocalDate.now();
        Long waitingOrders=orderMapper.countByStatus(OrdersDTO.TO_BE_CONFIRMED,now);
        OrderCountDTO orderCountDTO=orderMapper.getOrderList(begin,end);
        Long completedOrders=orderCountDTO.getValidOrderCount();
        Long allOrders=orderCountDTO.getOrderCount();
        Long deliveredOrders=orderMapper.countByStatus(OrdersDTO.CONFIRMED,now);
        Long cancelledOrders=orderMapper.countByStatus(OrdersDTO.CANCELLED,now);
        return OrderOverViewVO.builder().waitingOrders(waitingOrders).deliveredOrders(deliveredOrders).completedOrders(completedOrders).cancelledOrders(cancelledOrders).allOrders(allOrders).build();
    }
}
