package com.vibe.common.core.saga.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * SAGA 指标收集器
 * 使用 Micrometer 收集 SAGA 相关指标
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaMetrics {
    
    @Autowired(required = false)
    private MeterRegistry meterRegistry;
    
    /**
     * 事务计数器（按状态）
     */
    private final ConcurrentHashMap<String, Counter> transactionCounters = new ConcurrentHashMap<>();
    
    /**
     * 步骤计数器（按状态）
     */
    private final ConcurrentHashMap<String, Counter> stepCounters = new ConcurrentHashMap<>();
    
    /**
     * 事务耗时计时器
     */
    private Timer transactionTimer;
    
    /**
     * 步骤耗时计时器
     */
    private Timer stepTimer;
    
    /**
     * 补偿计数器
     */
    private Counter compensationCounter;
    
    /**
     * 超时事务计数器
     */
    private Counter timeoutTransactionCounter;
    
    @PostConstruct
    public void init() {
        if (meterRegistry == null) {
            log.warn("MeterRegistry 未配置，SAGA 指标收集功能将不可用");
            return;
        }
        
        // 初始化计时器
        transactionTimer = Timer.builder("saga.transaction.duration")
                .description("SAGA 事务执行耗时")
                .register(meterRegistry);
        
        stepTimer = Timer.builder("saga.step.duration")
                .description("SAGA 步骤执行耗时")
                .register(meterRegistry);
        
        // 初始化计数器
        compensationCounter = Counter.builder("saga.compensation.count")
                .description("SAGA 补偿次数")
                .register(meterRegistry);
        
        timeoutTransactionCounter = Counter.builder("saga.transaction.timeout.count")
                .description("SAGA 超时事务数")
                .register(meterRegistry);
        
        log.info("SAGA 指标收集器初始化完成");
    }
    
    /**
     * 记录事务创建
     * 
     * @param businessType 业务类型
     */
    public void recordTransactionCreated(String businessType) {
        if (meterRegistry == null) {
            return;
        }
        
        Counter counter = transactionCounters.computeIfAbsent("created", 
                k -> Counter.builder("saga.transaction.count")
                        .tag("status", "created")
                        .tag("business_type", businessType != null ? businessType : "unknown")
                        .description("SAGA 事务创建数")
                        .register(meterRegistry));
        counter.increment();
    }
    
    /**
     * 记录事务完成
     * 
     * @param businessType 业务类型
     * @param success 是否成功
     * @param durationMs 耗时（毫秒）
     */
    public void recordTransactionCompleted(String businessType, boolean success, long durationMs) {
        if (meterRegistry == null) {
            return;
        }
        
        String status = success ? "success" : "failed";
        Counter counter = transactionCounters.computeIfAbsent(status, 
                k -> Counter.builder("saga.transaction.count")
                        .tag("status", status)
                        .tag("business_type", businessType != null ? businessType : "unknown")
                        .description("SAGA 事务完成数")
                        .register(meterRegistry));
        counter.increment();
        
        if (transactionTimer != null) {
            transactionTimer.record(durationMs, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * 记录步骤执行
     * 
     * @param serviceName 服务名称
     * @param stepName 步骤名称
     * @param success 是否成功
     * @param durationMs 耗时（毫秒）
     */
    public void recordStepExecuted(String serviceName, String stepName, boolean success, long durationMs) {
        if (meterRegistry == null) {
            return;
        }
        
        String status = success ? "success" : "failed";
        String key = serviceName + ":" + stepName + ":" + status;
        
        Counter counter = stepCounters.computeIfAbsent(key, 
                k -> Counter.builder("saga.step.count")
                        .tag("service", serviceName)
                        .tag("step", stepName)
                        .tag("status", status)
                        .description("SAGA 步骤执行数")
                        .register(meterRegistry));
        counter.increment();
        
        if (stepTimer != null) {
            stepTimer.record(durationMs, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * 记录补偿
     * 
     * @param serviceName 服务名称
     * @param stepName 步骤名称
     */
    public void recordCompensation(String serviceName, String stepName) {
        if (meterRegistry == null || compensationCounter == null) {
            return;
        }
        
        compensationCounter.increment();
    }
    
    /**
     * 记录超时事务
     */
    public void recordTimeoutTransaction() {
        if (meterRegistry == null || timeoutTransactionCounter == null) {
            return;
        }
        
        timeoutTransactionCounter.increment();
    }
}
