package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("orderAdminController")
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
//    查询
    @GetMapping("/conditionSearch")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO){
        return Result.success(orderService.page(ordersPageQueryDTO));
    }
//    查看订单
    @GetMapping("/details/{id}")
    public Result<OrdersDTO> detail(@PathVariable Long id){
        OrdersDTO  res = orderService.getOrderDetail(id);
        return Result.success(res);
    }

//    接单
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrderDefult orderDefult){
        orderService.confirm(orderDefult);
        return Result.success();
    }


//    拒单
    @PutMapping("/rejection")
    public Result reject(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        orderService.reject(ordersRejectionDTO);
        return Result.success();
    }
//    派送
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }
//    完成
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        orderService.adminCancel(ordersCancelDTO);
        return Result.success();
    }
//   统计各个状态订单数量
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO=orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

}
