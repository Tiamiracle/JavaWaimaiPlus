package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.EmployeeService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO pageQueryDTO) {
        log.info("分页查询:{}", pageQueryDTO);
        return Result.success(categoryService.page(pageQueryDTO));
    }
    /**
     * 新增分类
     * POST/admin/category
     * 默认为0禁用
     */
    @PostMapping("")
    @ApiOperation("新增分类")
    public Result add(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类:{}", categoryDTO);
        categoryService.add(categoryDTO);
        return Result.success();
    }
    /**
     *修改分类
     * PUT/admin/category
     */
    @PutMapping("")
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类:{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }
    /**
     * 修改状态
     * POST/admin/category/status/{status}
     * id+status
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改状态")
    public Result changeStatus(@PathVariable("status") Integer status, @RequestParam("id") Long id) {
        log.info("启用禁用:{},status:{}", id, status);
        categoryService.changeStatus(status,id);
        return Result.success();
    }
    /**
     * DeleteMapping/admin/category
     */
    @DeleteMapping("")
    @ApiOperation("删除分类")
    public Result delete(@RequestParam("id") Long id) {
        log.info("删除分类:{}", id);
        categoryService.delete(id);
        return Result.success();
    }
    /**
     * 根据类型查分类
     * GET/admin/category/list
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查分类")
    public Result list(@RequestParam("type") Integer type) {
        log.info("根据类型查分类:{}", type);
        return Result.success( categoryService.list(type));
    }
}
