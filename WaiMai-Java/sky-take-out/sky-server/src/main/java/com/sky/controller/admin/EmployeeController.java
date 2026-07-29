package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     * 账号存在->异常
     * @return
     */
    @PostMapping("")
    @ApiOperation("新增员工")
    public Result<String> add(@RequestBody EmployeeDTO employee) {
        log.info("新增员工：{}", employee);
        employeeService.add(employee);
        return Result.success();
    }
    /**
     * 分页查询员工信息
     */
    @GetMapping("/page")
    @ApiOperation("分页查询员工信息")
    public Result<PageResult> page(EmployeePageQueryDTO pageQueryDTO) {
        log.info("分页查询员工信息，{}", pageQueryDTO);
        PageResult result=employeeService.page(pageQueryDTO);
        return Result.success(result);
    }
    /**
     * 启用禁用
     * 传入id+status，更改状态
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用")
    public Result changeStatus(@PathVariable("status") Integer status, @RequestParam Long id) {
        log.info("启用禁用，id{},status:{}", id,status);
        employeeService.changeStatus(status,id);
        return Result.success();
    }
    /**
     * 根据id查询员工信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<Employee> getById(@PathVariable("id") Long id) {
        log.info("根据id查询员工信息，id:{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }
    /**
     * 编辑员工信息
     */
    @PutMapping("")
    @ApiOperation("编辑员工信息")
    public Result edit(@RequestBody EmployeeDTO employee) {
        log.info("编辑员工信息，{}", employee);
        employeeService.edit(employee);
        return Result.success();
    }
    /**
     * 修改密码
     * id+newPassword+oldPassword
     */
    @PutMapping("/editPassword")
    @ApiOperation("修改密码")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO ) {
        log.info("修改密码，{}", passwordEditDTO);
        employeeService.editPassword(passwordEditDTO);
        return Result.success();
    }
}
