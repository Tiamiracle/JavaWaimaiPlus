package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BaseException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * 判断-》基本信息+状态+默认密码+创建人id-》存到数据库
     * @param employee
     */
    public void add(EmployeeDTO employee) {
        String username = employee.getUsername();
        if(employeeMapper.getByUsername(username) != null){
            throw new BaseException(MessageConstant.ACCOUNT_FOUND);
        }
        else{
            Employee emp=new Employee();
            BeanUtils.copyProperties(employee,emp);
            emp.setStatus(StatusConstant.ENABLE);
            emp.setCreateUser(BaseContext.getCurrentId());
            emp.setUpdateUser(BaseContext.getCurrentId());
            emp.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
            employeeMapper.add(emp);
        }
    }

    @Override
    public PageResult page(EmployeePageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<Employee> employeeList = employeeMapper.pageQuery(pageQueryDTO.getName());
        List<Employee> records = employeeList.getResult();
        records.forEach(employee -> {employee.setPassword("******");});
        PageResult result = new PageResult(employeeList.getTotal(), employeeList.getResult());
        return result;
    }

    @Override
    public void changeStatus(Integer status, Long id) {
        Employee employee=employeeMapper.getById(id);
        employee.setStatus(status);
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.edit(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee emp= employeeMapper.getById(id);
        emp.setPassword("******");
        return emp;
    }

    @Override
    public void edit(EmployeeDTO employee) {
        Employee temp=employeeMapper.getById(employee.getId());
        //判断新账号是否唯一
        if(!(temp.getUsername().equals(employee.getUsername()))&&employeeMapper.getByUsername(employee.getUsername()) != null){
            throw new BaseException(MessageConstant.ACCOUNT_FOUND);
        }
        Employee emp=new Employee();
        BeanUtils.copyProperties(employee,emp);
        emp.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.edit(emp);

    }

    /**
     * 修改密码id+新+旧
     * 旧密码错误-》抛出异常
     * 新密码和旧密码一致-》抛出异常
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Employee employee=employeeMapper.getById(BaseContext.getCurrentId());
        String oldPassword=DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if(!oldPassword.equals(employee.getPassword()))
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        if(passwordEditDTO.getNewPassword().equals(passwordEditDTO.getOldPassword()))
            throw new BaseException(MessageConstant.PASSWORD_ERROR2);
        String psw=DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes());
        employee.setPassword(psw);
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.edit(employee);
    }

}
