package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    PageResult page(OrdersPageQueryDTO ordersPageQueryDTO);

    OrdersDTO getOrderDetail(Long id);

    void confirm(OrderDefult orderDefult);

    void reject(OrdersRejectionDTO ordersRejectionDTO);

    void delivery(Long id);

    PageResult userPage(OrdersPageQueryDTO ordersPageQueryDTO);

    void cancel(Long id);

    void complete(Long id);

    void adminCancel(OrdersCancelDTO ordersCancelDTO);

    void reminder(Long id);

    OrdersDTO repetition(Long id);

    OrderStatisticsVO statistics();
}
