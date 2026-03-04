package com.vibe.common.core.monitor.aspect;

import com.vibe.common.core.log.PerformanceLogger;
import com.vibe.common.core.log.TraceIdUtils;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.common.core.monitor.config.PerformanceMonitorProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * 性能监控切面
 * 自动监控标记了 @MonitorPerformance 的方法执行时间
 * 当执行时间超过阈值时，记录到性能日志
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class PerformanceMonitorAspect {
    
    @Autowired
    private PerformanceMonitorProperties monitorProperties;
    
    /**
     * 切点：标记了 @MonitorPerformance 的方法
     */
    @Pointcut("@annotation(com.vibe.common.core.monitor.annotation.MonitorPerformance)")
    public void monitorPerformancePointcut() {
    }
    
    /**
     * 环绕通知：监控方法执行时间
     * 
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("monitorPerformancePointcut()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        // 如果未启用监控，直接执行
        if (!monitorProperties.isEnabled()) {
            return joinPoint.proceed();
        }
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MonitorPerformance monitorPerformance = method.getAnnotation(MonitorPerformance.class);
        
        if (monitorPerformance == null) {
            return joinPoint.proceed();
        }
        
        // 获取配置信息
        long threshold = monitorPerformance.threshold() > 0 
                ? monitorPerformance.threshold() 
                : monitorProperties.getDefaultThreshold();
        String operation = monitorPerformance.operation();
        if (operation == null || operation.isEmpty()) {
            operation = method.getName();
        }
        boolean async = monitorPerformance.async();
        
        // 获取类名和方法名
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String fullMethodName = className + "." + methodName;
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        boolean success = true;
        Throwable exception = null;
        
        try {
            // 执行方法
            Object result = joinPoint.proceed();
            return result;
            
        } catch (Throwable e) {
            success = false;
            exception = e;
            throw e;
            
        } finally {
            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            
            // 判断是否需要记录
            boolean needRecord = monitorProperties.isRecordAll() || duration > threshold;
            
            if (needRecord) {
                // 异步或同步记录
                if (async) {
                    recordPerformanceAsync(className, methodName, fullMethodName, operation, 
                            duration, threshold, success);
                } else {
                    recordPerformance(className, methodName, fullMethodName, operation, 
                            duration, threshold, success);
                }
            }
        }
    }
    
    /**
     * 记录性能日志（同步）
     * 
     * @param className 类名
     * @param methodName 方法名
     * @param fullMethodName 完整方法名
     * @param operation 操作名称
     * @param duration 执行时间
     * @param threshold 阈值
     * @param success 是否成功
     */
    private void recordPerformance(String className, String methodName, String fullMethodName, 
                                   String operation, long duration, long threshold, boolean success) {
        try {
            PerformanceLogger.logMethodPerformance(className, methodName, operation, 
                    duration, threshold, success);
        } catch (Exception e) {
            log.error("记录性能日志失败，方法: {}", fullMethodName, e);
        }
    }
    
    /**
     * 记录性能日志（异步）
     * 
     * @param className 类名
     * @param methodName 方法名
     * @param fullMethodName 完整方法名
     * @param operation 操作名称
     * @param duration 执行时间
     * @param threshold 阈值
     * @param success 是否成功
     */
    private void recordPerformanceAsync(String className, String methodName, String fullMethodName, 
                                        String operation, long duration, long threshold, boolean success) {
        CompletableFuture.runAsync(() -> {
            try {
                // 异步执行时，需要重新设置 TraceId
                String traceId = TraceIdUtils.getTraceIdOrNull();
                if (traceId == null || traceId.isEmpty()) {
                    TraceIdUtils.setTraceId(TraceIdUtils.generateTraceId());
                }
                
                recordPerformance(className, methodName, fullMethodName, operation, 
                        duration, threshold, success);
            } catch (Exception e) {
                log.error("异步记录性能日志失败，方法: {}", fullMethodName, e);
            }
        });
    }
}
