package com.vibe.common.core.log.aspect;

import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 操作日志切面
 * 用于记录操作审计日志
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Aspect
@Component
public class LogOperationAspect {
    
    /**
     * 切点：标记了 @LogOperation 的方法
     */
    @Pointcut("@annotation(com.vibe.common.core.log.annotation.LogOperation)")
    public void logOperationPointcut() {
    }
    
    /**
     * 环绕通知：记录操作日志
     * 
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("logOperationPointcut()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogOperation logOperation = method.getAnnotation(LogOperation.class);
        
        if (logOperation == null) {
            return joinPoint.proceed();
        }
        
        String operation = logOperation.operation();
        String resource = logOperation.resource();
        String action = logOperation.action();
        boolean recordParams = logOperation.recordParams();
        boolean recordResult = logOperation.recordResult();
        
        // 提取资源ID（从方法参数中）
        String resourceId = extractResourceId(joinPoint.getArgs());
        
        long startTime = System.currentTimeMillis();
        String result = "SUCCESS";
        String remark = "";
        
        try {
            // 记录操作开始
            if (recordParams) {
                String params = formatParams(joinPoint.getArgs());
                remark = "PARAMS:" + params;
            }
            
            Object returnValue = joinPoint.proceed();
            
            // 记录操作成功
            if (recordResult && returnValue != null) {
                remark += "|RESULT:" + formatResult(returnValue);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            remark += "|DURATION:" + duration + "ms";
            
            // 记录审计日志
            LogUtils.auditLog(operation, resource, resourceId, action, result, remark);
            
            return returnValue;
            
        } catch (Throwable e) {
            result = "FAILED";
            remark += "|ERROR:" + e.getMessage();
            
            long duration = System.currentTimeMillis() - startTime;
            remark += "|DURATION:" + duration + "ms";
            
            // 记录审计日志
            LogUtils.auditLog(operation, resource, resourceId, action, result, remark);
            
            throw e;
        }
    }
    
    /**
     * 提取资源ID
     * 
     * @param args 方法参数
     * @return 资源ID
     */
    private String extractResourceId(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        // 尝试从参数中提取ID
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            // 如果是Long类型，可能是ID
            if (arg instanceof Long) {
                return String.valueOf(arg);
            }
            // 如果是String类型，可能是ID
            if (arg instanceof String && ((String) arg).matches("\\d+")) {
                return (String) arg;
            }
            // 尝试通过反射获取id字段
            try {
                java.lang.reflect.Field idField = arg.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object id = idField.get(arg);
                if (id != null) {
                    return String.valueOf(id);
                }
            } catch (Exception e) {
                // 忽略
            }
        }
        return "";
    }
    
    /**
     * 格式化参数
     * 
     * @param args 参数数组
     * @return 格式化后的参数字符串
     */
    private String formatParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                sb.append(arg.getClass().getSimpleName()).append(":").append(arg).append(",");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    
    /**
     * 格式化结果
     * 
     * @param result 结果对象
     * @return 格式化后的结果字符串
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        return result.getClass().getSimpleName() + ":" + result.toString();
    }
}
