package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import com.sky.vo.RecommendDishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController("userDishController")
@RequestMapping("/user/dish")
public class DishController {
    /**
     * 获取推荐菜品
     * limit推荐菜品条数默认8
     * @param limit
     * @return
     */
    @GetMapping("/recommend/{userId}")
    @Cacheable(value = "dishRecommend", key = "#userId")
    public Result<List<RecommendDishVO>> recommend(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "8") Integer limit) {
        List<RecommendDishVO> list = dishService.recommend(userId, limit);
        return Result.success(list);
    }
    @Autowired
    private DishService dishService;
    @GetMapping("/list")
    @Cacheable(value = "dishCache", key = "#categoryId")//key:dishCache::categoryId
    public Result getList(@RequestParam Long categoryId){
        List<DishVO> list1=dishService.list(categoryId);
        List<DishVO> filteredList = list1.stream()
                .filter(item -> item.getStatus() != 0)
                .collect(Collectors.toList());
        return Result.success(filteredList);
    }

}
