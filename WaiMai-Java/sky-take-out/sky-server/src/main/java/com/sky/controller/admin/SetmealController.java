package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询套餐，{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐，id:{}", id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    /**
     * 新增套餐
     * POST/admin/setmeal
     */
    @ApiOperation("新增套餐")
    @PostMapping("")
    @CacheEvict(value = "dishCache", key = "#setmealDTO.categoryId")
    public Result add(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.add(setmealDTO);
        return Result.success();
    }

    /**
     * 修改套餐
     * PUT /admin/setmeal
     */
    @PutMapping("")
    @ApiOperation("修改套餐")
    @CacheEvict(value = "dishCache", key = "#setmealDTO.categoryId")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
    /**
     * 批量删除套餐
     * DELETE /admin/setmeal
     * ids
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(value = "dishCache", allEntries = true)
    public Result delete(@RequestParam String ids) {
        log.info("批量删除套餐，ids：{}", ids);
        setmealService.delete(ids);
        return Result.success();
    }
    /**
     * 修改状态
     * POST/admin/setmeal/status/{status}
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改状态")
    @CacheEvict(value = "dishCache", allEntries = true)
    public Result updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("修改状态，status：{}, ids：{}", status, id);
        setmealService.updateStatus(status, id);
        return Result.success();
    }
}
