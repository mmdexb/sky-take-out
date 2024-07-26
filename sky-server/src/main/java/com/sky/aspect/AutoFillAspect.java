package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){
    }

    @Before("autoFillPointCut()")
    public void autofill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充");
        MethodSignature signature =(MethodSignature) joinPoint.getSignature(); //获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); //获取方法上的注解对象
        OperationType operationType = autoFill.value(); //获得数据库操作类型

        Object[] args = joinPoint.getArgs();
        if (args.length == 0){
            return;
        }
        Object entry = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long id = Thread.currentThread().getId();

        if(operationType == OperationType.INSERT){
            try {
                Method setCreateTime = entry.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setCreateUser = entry.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateTime = entry.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entry.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                setCreateTime.invoke(entry,now);
                setCreateUser.invoke(entry,id);
                setUpdateTime.invoke(entry,now);
                setUpdateUser.invoke(entry,id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entry.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entry.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                setUpdateTime.invoke(entry,now);
                setUpdateUser.invoke(entry,id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
