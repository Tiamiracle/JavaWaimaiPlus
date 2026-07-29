package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/workspace")
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;



//    运营数据
    @RequestMapping("/businessData")
    public Result<BusinessDataVO> businessData() {
        LocalDate now=LocalDate.now();
        BusinessDataVO res=workspaceService.businessData(now,now);
        return Result.success(res);
    }

//    套餐数据
    @RequestMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        SetmealOverViewVO res=workspaceService.overviewSetmeals();
        return Result.success(res);
    }

//    菜品数据
    @RequestMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes() {
        DishOverViewVO res=workspaceService.overviewDishes();
        return Result.success(res);
    }

//    订单数据
    @RequestMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders() {
        LocalDate now=LocalDate.now();
        OrderOverViewVO res=workspaceService.overviewOrders(now,now);
        return Result.success(res);
    }
}
