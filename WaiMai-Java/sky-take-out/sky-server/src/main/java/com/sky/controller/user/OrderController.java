package com.sky.controller.user;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
//    下单
    @PostMapping ("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        OrderSubmitVO orderSubmitVO=orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
//    支付订单
    @PutMapping("/payment")
    public Result<OrderPaymentVO> pay(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        OrderPaymentVO vo = orderService.payment(ordersPaymentDTO);
        return Result.success(vo);
    }
//    查看历史订单
    @GetMapping("/historyOrders")
    public Result<PageResult> history(OrdersPageQueryDTO ordersPageQueryDTO){
        return Result.success(orderService.userPage(ordersPageQueryDTO));
    }
//    查看订单详情
    @GetMapping("/orderDetail/{id}")
    public Result<OrdersDTO> detail(@PathVariable Long id){
        OrdersDTO res = orderService.getOrderDetail(id);
        return Result.success(res);
    }
//    取消订单
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id){
        orderService.cancel(id);
        return Result.success();
    }

//    再来一单
    @PostMapping("/repetition/{id}")
    public Result<OrdersDTO> repetition(@PathVariable Long id){
        return Result.success(orderService.repetition(id));
    }
//    催单
    @GetMapping("/reminder/{id}")
    public Result<OrderSubmitVO>reminder(@PathVariable Long id){
        orderService.reminder(id);
        return Result.success();
    }

}
