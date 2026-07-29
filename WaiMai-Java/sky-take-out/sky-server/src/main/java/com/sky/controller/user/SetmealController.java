package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#categoryId")//key:dishCache::categoryId
    public Result getList(@RequestParam Long categoryId){
        List<SetmealVO>list1=setmealService.list(categoryId);
        List<SetmealVO> filteredList = list1.stream()
                .filter(item -> item.getStatus() != 0)
                .collect(Collectors.toList());
        return Result.success(filteredList);
    }
    //查看具体套餐
    @GetMapping("/dish/{id}")
    public Result getDish(@PathVariable Long id){
        return Result.success(setmealService.getById(id).getSetmealDishes());
    }
}
