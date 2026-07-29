package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController("userCategoryController")
@RequestMapping("/user/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping("/list")
    public Result getList(){
        List<Category> list1 = categoryService.list(1);
        List<Category> list2 = categoryService.list(2);
        list1.addAll(list2);
        List<Category> filteredList = list1.stream()
                .filter(item -> item.getStatus() != 0)
                .collect(Collectors.toList());
        return Result.success(filteredList);
    }
}
