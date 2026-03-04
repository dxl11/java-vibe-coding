package com.vibe.common.core.saga.retry;

import com.vibe.common.core.saga.SagaTransactionManager;
import com.vibe.common.core.saga.config.SagaProperties;
import com.vibe.common.core.saga.entity.SagaStep;
import com.vibe.common.core.saga.mapper.SagaStepMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SAGA 重试处理器
 * 负责自动重试失败的步骤
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaRetryHandler {
    
    @Autowired
    private SagaStepMapper sagaStepMapper;
    
    @Autowired
    private SagaTransactionManager sagaTransactionManager;
    
    @Autowired
    private SagaProperties sagaProperties;
    
    /**
     * 查询需要重试的步骤
     * 
     * @param limit 查询数量限制
     * @return 需要重试的步骤列表
     */
    public List<SagaStep> getRetryableSteps(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        
        LocalDateTime currentTime = LocalDateTime.now();
        return sagaStepMapper.selectRetryableSteps(currentTime, limit);
    }
    
    /**
     * 判断步骤是否需要重试
     * 
     * @param step 步骤
     * @return 是否需要重试
     */
    public boolean shouldRetry(SagaStep step) {
        if (step == null || step.getStatus() != SagaStep.StepStatus.FAILED.getCode()) {
            return false;
        }
        
        if (step.getRetryCount() == null) {
            step.setRetryCount(0);
        }
        if (step.getMaxRetryCount() == null) {
            step.setMaxRetryCount(sagaProperties.getRetry().getMaxRetryCount());
        }
        
        return step.getRetryCount() < step.getMaxRetryCount();
    }
    
    /**
     * 获取重试策略
     * 
     * @param step 步骤
     * @return 重试策略
     */
    private SagaRetryPolicy getRetryPolicy(SagaStep step) {
        SagaProperties.RetryConfig retryConfig = sagaProperties.getRetry();
        return SagaRetryPolicy.builder()
                .maxRetryCount(step.getMaxRetryCount() != null ? step.getMaxRetryCount() : retryConfig.getMaxRetryCount())
                .initialIntervalMs(retryConfig.getInitialIntervalMs())
                .maxIntervalMs(retryConfig.getMaxIntervalMs())
                .multiplier(retryConfig.getMultiplier())
                .retryableExceptions(retryConfig.getRetryableExceptions())
                .build();
    }
    
    /**
     * 标记步骤需要重试
     * 
     * @param stepId 步骤ID
     * @param nextRetryTime 下次重试时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void markForRetry(String stepId, LocalDateTime nextRetryTime) {
        sagaStepMapper.incrementRetryCount(stepId, nextRetryTime);
        log.info("标记步骤需要重试，StepId: {}, NextRetryTime: {}", stepId, nextRetryTime);
    }
    
    /**
     * 处理步骤重试
     * 
     * @param step 步骤
     * @param retryExecutor 重试执行逻辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void retryStep(SagaStep step, java.util.function.Supplier<Boolean> retryExecutor) {
        if (!shouldRetry(step)) {
            log.warn("步骤已达到最大重试次数，不再重试，StepId: {}", step.getStepId());
            return;
        }
        
        SagaRetryPolicy retryPolicy = getRetryPolicy(step);
        int retryCount = step.getRetryCount() != null ? step.getRetryCount() : 0;
        
        try {
            log.info("开始重试步骤，StepId: {}, RetryCount: {}", step.getStepId(), retryCount + 1);
            
            // 执行重试
            Boolean success = retryExecutor.get();
            
            if (success != null && success) {
                // 重试成功，更新步骤状态
                sagaTransactionManager.recordStepSuccess(step.getStepId(), "RETRY_SUCCESS", null);
                log.info("步骤重试成功，StepId: {}", step.getStepId());
            } else {
                // 重试失败，计算下次重试时间
                long nextInterval = retryPolicy.calculateNextInterval(retryCount + 1);
                LocalDateTime nextRetryTime = LocalDateTime.now().plusNanos(nextInterval * 1_000_000);
                markForRetry(step.getStepId(), nextRetryTime);
                log.warn("步骤重试失败，StepId: {}, NextRetryTime: {}", step.getStepId(), nextRetryTime);
            }
            
        } catch (Exception e) {
            // 判断异常是否可重试
            if (retryPolicy.isRetryable(e)) {
                long nextInterval = retryPolicy.calculateNextInterval(retryCount + 1);
                LocalDateTime nextRetryTime = LocalDateTime.now().plusNanos(nextInterval * 1_000_000);
                markForRetry(step.getStepId(), nextRetryTime);
                log.warn("步骤重试异常，StepId: {}, NextRetryTime: {}", step.getStepId(), nextRetryTime, e);
            } else {
                log.error("步骤重试异常且不可重试，StepId: {}", step.getStepId(), e);
            }
        }
    }
}
