package com.sky.aspect;

import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 自定义切面类
 */
@Aspect
@Component
public class AutoFillAspect {

    /**
     * 切入点：匹配 mapper 层所有带 @AutoFill 注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.aspect.AutoFill)")
    public void autoFillPointCut() {}

    /**
     * 前置通知：在执行 SQL 之前自动填充字段
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        // 获取当前登录用户ID
        Long currentId = BaseContext.getCurrentId();

        // 获取方法签名、注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType type = autoFill.value();

        // 获取方法参数（实体对象）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) return;
        Object entity = args[0];

        try {
            // 公共字段
            if (type==OperationType.INSERT) {
                // 新增：填充 创建人、创建时间、修改人、修改时间
                entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class).invoke(entity, currentId);
                entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(entity, currentId);
            } else if (type==OperationType.UPDATE) {
                // 修改：填充 修改人、修改时间
                entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(entity, currentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
