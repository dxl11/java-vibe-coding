package com.vibe.common.core.saga;

import com.vibe.common.core.saga.circuit.SagaCircuitBreaker;
import com.vibe.common.core.saga.entity.SagaStep;
import com.vibe.common.core.saga.metrics.SagaMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * SAGA 步骤执行器
 * 负责执行步骤并记录执行结果
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaStepExecutor {
    
    @Autowired
    private SagaTransactionManager sagaTransactionManager;
    
    @Autowired(required = false)
    private SagaCircuitBreaker sagaCircuitBreaker;
    
    @Autowired(required = false)
    private SagaMetrics sagaMetrics;
    
    /**
     * 执行步骤
     * 
     * @param stepId 步骤ID
     * @param serviceName 服务名称
     * @param stepName 步骤名称
     * @param stepExecutor 步骤执行逻辑
     * @return 执行结果
     */
    public <T> T executeStep(String stepId, String serviceName, String stepName, Supplier<T> stepExecutor) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始执行步骤，StepId: {}, ServiceName: {}, StepName: {}", stepId, serviceName, stepName);
            
            // 使用熔断器保护
            Supplier<T> protectedExecutor = stepExecutor;
            if (sagaCircuitBreaker != null) {
                protectedExecutor = () -> sagaCircuitBreaker.executeWithCircuitBreaker(serviceName, stepName, stepExecutor);
            }
            
            // 执行步骤逻辑
            T result = protectedExecutor.get();
            
            // 计算执行耗时
            long durationMs = System.currentTimeMillis() - startTime;
            
            // 记录执行成功
            String responseData = result != null ? result.toString() : "SUCCESS";
            sagaTransactionManager.recordStepSuccess(stepId, responseData, durationMs);
            
            // 记录指标
            if (sagaMetrics != null) {
                sagaMetrics.recordStepExecuted(serviceName, stepName, true, durationMs);
            }
            
            log.info("步骤执行成功，StepId: {}, Duration: {}ms", stepId, durationMs);
            
            return result;
            
        } catch (Exception e) {
            // 计算执行耗时
            long durationMs = System.currentTimeMillis() - startTime;
            
            // 记录执行失败
            sagaTransactionManager.recordStepFailure(stepId, e.getMessage());
            
            // 记录指标
            if (sagaMetrics != null) {
                sagaMetrics.recordStepExecuted(serviceName, stepName, false, durationMs);
            }
            
            log.error("步骤执行失败，StepId: {}, Duration: {}ms", stepId, durationMs, e);
            
            throw e;
        }
    }
    
    /**
     * 执行步骤（兼容旧接口）
     * 
     * @param stepId 步骤ID
     * @param stepExecutor 步骤执行逻辑
     * @return 执行结果
     */
    public <T> T executeStep(String stepId, Supplier<T> stepExecutor) {
        return executeStep(stepId, "unknown", "unknown", stepExecutor);
    }
    
    /**
     * 执行步骤（无返回值）
     * 
     * @param stepId 步骤ID
     * @param stepExecutor 步骤执行逻辑
     */
    public void executeStep(String stepId, Runnable stepExecutor) {
        executeStep(stepId, () -> {
            stepExecutor.run();
            return null;
        });
    }
    
    /**
     * 执行补偿
     * 
     * @param stepId 步骤ID
     * @param compensationExecutor 补偿执行逻辑
     */
    public void executeCompensation(String stepId, Runnable compensationExecutor) {
        try {
            log.info("开始执行补偿，StepId: {}", stepId);
            
            // 执行补偿逻辑
            compensationExecutor.run();
            
            // 记录补偿完成
            sagaTransactionManager.recordStepCompensation(stepId);
            
            log.info("补偿执行成功，StepId: {}", stepId);
            
        } catch (Exception e) {
            log.error("补偿执行失败，StepId: {}", stepId, e);
            throw e;
        }
    }
}
