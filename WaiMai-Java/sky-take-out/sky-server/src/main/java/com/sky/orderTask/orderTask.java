package com.sky.orderTask;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单状态的定时更新
 * 用户下单15分钟之内用户未支付则订单状态更新为已取消，取消原因是订单支付已超时
 * 每天凌晨0点将所有派送中的订单状态更新为已完成
 */
@Component
public class orderTask {
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron = "* * * * * ?")
    public void orderOutTime() {
        //定时任务
        LocalDateTime timeThreshold = LocalDateTime.now().plusMinutes(-14);
        List<Orders> timeoutOrderList = orderMapper.getTimeoutUnpaidOrders(Orders.PENDING_PAYMENT, timeThreshold);
        if (timeoutOrderList == null || timeoutOrderList.isEmpty()) {
            return;
        }
        for (Orders order : timeoutOrderList) {
            order.setStatus(Orders.CANCELLED);
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason("订单支付超时，系统自动取消");
            orderMapper.update(order);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void orderDeliver() {
        //定时任务
        List<Orders> deliverOrderList = orderMapper.listByStatus(Orders.DELIVERY_IN_PROGRESS);
        if (deliverOrderList == null || deliverOrderList.isEmpty()) {
            return;
        }
        for (Orders order : deliverOrderList) {
            order.setStatus(Orders.COMPLETED);
            order.setDeliveryTime(LocalDateTime.now());
            orderMapper.update(order);
        }
    }
}
