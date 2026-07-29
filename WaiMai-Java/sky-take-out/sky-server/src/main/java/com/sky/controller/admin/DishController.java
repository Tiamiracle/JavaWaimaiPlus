package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理接口
 */
@RestController
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    /**
     * 分页查找
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        PageResult result= dishService.pageQuery(dishPageQueryDTO);
        return Result.success(result);
    }
    /**
     * 新增菜品
     * POST/admin/dish
     */
    @PostMapping("")
    @ApiOperation("新增菜品")
    @CacheEvict(value = "dishCache", key = "#dishDTO.categoryId")
    public Result add(@RequestBody DishDTO dishDTO) {
        dishService.add(dishDTO);
        return Result.success();
    }
    /**
     * 修改菜品
     * 修改之后的名字不能重复
     * PUT/admin/dish
     */
    @PutMapping("")
    @ApiOperation("修改菜品")
    @CacheEvict(value = "dishCache", key = "#dishDTO.categoryId")
    public Result update(@RequestBody DishDTO dishDTO) {
        dishService.update(dishDTO);
        return Result.success();
    }
    /**
     * 根据id查询菜品
     * GET/admin/dish/{id}
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVo=dishService.getById(id);
        return Result.success(dishVo);
    }
    /**
     * 修改状态
     * POST/admin/dish/status/{status}
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改状态")
    @CacheEvict(value = "dishCache", allEntries = true)
    public Result updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        dishService.updateStatus(status,id);
        return Result.success();
    }
    /**
     * 删除
     * DELETE/admin/dish
     */
    @DeleteMapping("")
    @ApiOperation("批量删除")
    @CacheEvict(value = "dishCache", allEntries = true)
    public Result delete(@RequestParam String ids) {
        dishService.delete(ids);
        return Result.success();
    }
    /**
     * 根据分类id查询菜品
     * GET/admin/dish/list
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result list(@RequestParam Long categoryId) {
        List<DishVO> dishVoList=dishService.list(categoryId);
        return Result.success(dishVoList);
    }


}
