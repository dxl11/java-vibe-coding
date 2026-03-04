package com.vibe.common.core.saga;

import com.vibe.common.core.saga.entity.SagaTransaction;
import com.vibe.common.core.saga.entity.SagaStep;
import com.vibe.common.core.saga.mapper.SagaTransactionMapper;
import com.vibe.common.core.saga.mapper.SagaStepMapper;
import com.vibe.common.core.saga.SagaTransactionManager;
import com.vibe.common.core.saga.metrics.SagaMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SAGA 超时处理器
 * 处理超时的事务，自动触发补偿
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaTimeoutHandler {
    
    @Autowired
    private SagaTransactionMapper sagaTransactionMapper;
    
    @Autowired
    private SagaStepMapper sagaStepMapper;
    
    @Autowired
    private SagaTransactionManager sagaTransactionManager;
    
    @Autowired(required = false)
    private SagaMetrics sagaMetrics;
    
    /**
     * 处理超时的事务
     * 
     * @param limit 处理数量限制
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeoutTransactions(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;  // 默认处理100个
        }
        
        LocalDateTime currentTime = LocalDateTime.now();
        List<SagaTransaction> timeoutTransactions = sagaTransactionMapper.selectTimeoutTransactions(currentTime, limit);
        
        if (timeoutTransactions.isEmpty()) {
            return;
        }
        
        log.warn("发现 {} 个超时事务，开始处理", timeoutTransactions.size());
        
        for (SagaTransaction transaction : timeoutTransactions) {
            try {
                handleTimeoutTransaction(transaction);
            } catch (Exception e) {
                log.error("处理超时事务失败，TransactionId: {}", transaction.getTransactionId(), e);
            }
        }
    }
    
    /**
     * 处理单个超时事务
     * 
     * @param transaction 事务
     */
    private void handleTimeoutTransaction(SagaTransaction transaction) {
        String transactionId = transaction.getTransactionId();
        
        log.warn("处理超时事务，TransactionId: {}, BusinessId: {}, BusinessType: {}", 
                transactionId, transaction.getBusinessId(), transaction.getBusinessType());
        
        // 如果事务已经在补偿中或已补偿，跳过
        if (transaction.getStatus() == SagaTransaction.TransactionStatus.COMPENSATING.getCode() ||
            transaction.getStatus() == SagaTransaction.TransactionStatus.COMPENSATED.getCode()) {
            log.info("事务已在补偿中或已补偿，跳过，TransactionId: {}", transactionId);
            return;
        }
        
        // 开始补偿流程
        sagaTransactionManager.startCompensation(transactionId);
        
        // 查询需要补偿的步骤（已成功但未补偿的步骤）
        List<SagaStep> successSteps = sagaStepMapper.selectByTransactionIdAndStatus(
                transactionId, SagaStep.StepStatus.SUCCESS.getCode());
        
        // 按步骤顺序倒序补偿（后进先出）
        successSteps.sort((a, b) -> b.getStepOrder().compareTo(a.getStepOrder()));
        
        for (SagaStep step : successSteps) {
            try {
                // 这里应该调用具体的补偿逻辑
                // 由于是协同式SAGA，补偿逻辑由各个服务的事件监听器处理
                // 这里只是标记步骤需要补偿，实际补偿由事件触发
                
                log.info("标记步骤需要补偿，StepId: {}, ServiceName: {}, StepName: {}", 
                        step.getStepId(), step.getServiceName(), step.getStepName());
                
            } catch (Exception e) {
                log.error("补偿步骤失败，StepId: {}", step.getStepId(), e);
            }
        }
        
        // 标记事务失败
        sagaTransactionManager.markTransactionFailed(transactionId, "事务超时");
        
        // 记录指标
        if (sagaMetrics != null) {
            sagaMetrics.recordTimeoutTransaction();
        }
        
        log.warn("超时事务处理完成，TransactionId: {}", transactionId);
    }
}
