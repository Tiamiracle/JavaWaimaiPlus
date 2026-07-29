package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDate;

public interface WorkspaceService {
    SetmealOverViewVO overviewSetmeals();

    BusinessDataVO businessData(LocalDate begin, LocalDate end);

    DishOverViewVO overviewDishes();

    OrderOverViewVO overviewOrders(LocalDate begin, LocalDate end);
}
