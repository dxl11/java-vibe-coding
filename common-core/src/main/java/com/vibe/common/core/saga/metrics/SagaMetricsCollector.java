package com.vibe.common.core.saga.metrics;

import com.vibe.common.core.saga.SagaTransactionManager;
import com.vibe.common.core.saga.config.SagaProperties;
import com.vibe.common.core.saga.entity.SagaTransaction;
import com.vibe.common.core.saga.entity.SagaStep;
import com.vibe.common.core.saga.mapper.SagaTransactionMapper;
import com.vibe.common.core.saga.mapper.SagaStepMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SAGA 指标收集器
 * 定期收集 SAGA 相关指标
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaMetricsCollector {
    
    @Autowired
    private SagaTransactionMapper sagaTransactionMapper;
    
    @Autowired
    private SagaStepMapper sagaStepMapper;
    
    @Autowired
    private SagaMetrics sagaMetrics;
    
    @Autowired
    private SagaProperties sagaProperties;
    
    /**
     * 定期收集指标
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void collectMetrics() {
        if (!sagaProperties.getMetrics().getEnabled()) {
            return;
        }
        
        try {
            // 收集事务指标
            collectTransactionMetrics();
            
            // 收集步骤指标
            collectStepMetrics();
            
        } catch (Exception e) {
            log.error("收集 SAGA 指标异常", e);
        }
    }
    
    /**
     * 收集事务指标
     */
    private void collectTransactionMetrics() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        
        // 查询最近1分钟的事务
        List<SagaTransaction> recentTransactions = sagaTransactionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaTransaction>()
                        .ge(SagaTransaction::getCreateTime, oneMinuteAgo)
                        .eq(SagaTransaction::getIsDeleted, 0)
        );
        
        for (SagaTransaction transaction : recentTransactions) {
            SagaTransaction.TransactionStatus status = 
                    SagaTransaction.TransactionStatus.getByCode(transaction.getStatus());
            
            if (status == SagaTransaction.TransactionStatus.COMPLETED) {
                sagaMetrics.recordTransactionCompleted(transaction.getBusinessType(), true, 0);
            } else if (status == SagaTransaction.TransactionStatus.FAILED ||
                       status == SagaTransaction.TransactionStatus.COMPENSATED) {
                sagaMetrics.recordTransactionCompleted(transaction.getBusinessType(), false, 0);
            }
        }
    }
    
    /**
     * 收集步骤指标
     */
    private void collectStepMetrics() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        
        // 查询最近1分钟的步骤
        List<SagaStep> recentSteps = sagaStepMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaStep>()
                        .ge(SagaStep::getCreateTime, oneMinuteAgo)
                        .eq(SagaStep::getIsDeleted, 0)
        );
        
        for (SagaStep step : recentSteps) {
            SagaStep.StepStatus status = SagaStep.StepStatus.getByCode(step.getStatus());
            
            boolean success = status == SagaStep.StepStatus.SUCCESS;
            long durationMs = step.getDurationMs() != null ? step.getDurationMs() : 0;
            
            sagaMetrics.recordStepExecuted(step.getServiceName(), step.getStepName(), success, durationMs);
            
            if (status == SagaStep.StepStatus.COMPENSATED) {
                sagaMetrics.recordCompensation(step.getServiceName(), step.getStepName());
            }
        }
    }
}
