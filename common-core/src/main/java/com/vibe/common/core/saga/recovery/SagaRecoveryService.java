package com.vibe.common.core.saga.recovery;

import com.vibe.common.core.saga.SagaTransactionManager;
import com.vibe.common.core.saga.config.SagaProperties;
import com.vibe.common.core.saga.entity.SagaTransaction;
import com.vibe.common.core.saga.entity.SagaStep;
import com.vibe.common.core.saga.mapper.SagaTransactionMapper;
import com.vibe.common.core.saga.mapper.SagaStepMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SAGA 事务恢复服务
 * 支持手动和自动恢复失败的事务
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class SagaRecoveryService {
    
    @Autowired
    private SagaTransactionMapper sagaTransactionMapper;
    
    @Autowired
    private SagaStepMapper sagaStepMapper;
    
    @Autowired
    private SagaTransactionManager sagaTransactionManager;
    
    @Autowired
    private SagaProperties sagaProperties;
    
    /**
     * 手动恢复事务
     * 
     * @param transactionId 事务ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean recoverTransaction(String transactionId) {
        log.info("开始恢复事务，TransactionId: {}", transactionId);
        
        SagaTransaction transaction = sagaTransactionManager.getTransaction(transactionId);
        if (transaction == null) {
            log.warn("事务不存在，TransactionId: {}", transactionId);
            return false;
        }
        
        // 只有失败或补偿中的事务才能恢复
        if (transaction.getStatus() != SagaTransaction.TransactionStatus.FAILED.getCode() &&
            transaction.getStatus() != SagaTransaction.TransactionStatus.COMPENSATING.getCode()) {
            log.warn("事务状态不允许恢复，TransactionId: {}, Status: {}", 
                    transactionId, transaction.getStatus());
            return false;
        }
        
        // 查询失败的步骤
        List<SagaStep> failedSteps = sagaStepMapper.selectByTransactionIdAndStatus(
                transactionId, SagaStep.StepStatus.FAILED.getCode());
        
        if (failedSteps.isEmpty()) {
            log.info("没有失败的步骤，无需恢复，TransactionId: {}", transactionId);
            return true;
        }
        
        // 重新执行失败的步骤
        for (SagaStep step : failedSteps) {
            try {
                log.info("恢复步骤，StepId: {}, ServiceName: {}, StepName: {}", 
                        step.getStepId(), step.getServiceName(), step.getStepName());
                
                // 重置步骤状态为待执行
                step.setStatus(SagaStep.StepStatus.PENDING.getCode());
                step.setRetryCount(0);
                step.setNextRetryTime(null);
                sagaStepMapper.updateById(step);
                
                // TODO: 触发步骤重新执行
                // 由于是协同式SAGA，需要通过事件触发
                
            } catch (Exception e) {
                log.error("恢复步骤失败，StepId: {}", step.getStepId(), e);
            }
        }
        
        // 更新事务状态为处理中
        sagaTransactionManager.updateTransactionStatus(
                transactionId, 
                SagaTransaction.TransactionStatus.PROCESSING);
        
        log.info("事务恢复完成，TransactionId: {}", transactionId);
        return true;
    }
    
    /**
     * 自动恢复失败的事务
     * 
     * @param limit 恢复数量限制
     */
    @Transactional(rollbackFor = Exception.class)
    public void autoRecoverTransactions(Integer limit) {
        if (!sagaProperties.getRecovery().getAutoRecoveryEnabled()) {
            return;
        }
        
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        
        // 查询失败的事务（最近1小时内的）
        java.time.LocalDateTime oneHourAgo = java.time.LocalDateTime.now().minusHours(1);
        
        List<SagaTransaction> failedTransactions = sagaTransactionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaTransaction>()
                        .eq(SagaTransaction::getStatus, SagaTransaction.TransactionStatus.FAILED.getCode())
                        .ge(SagaTransaction::getCreateTime, oneHourAgo)
                        .eq(SagaTransaction::getIsDeleted, 0)
                        .orderByAsc(SagaTransaction::getCreateTime)
                        .last("LIMIT " + limit)
        );
        
        if (failedTransactions.isEmpty()) {
            return;
        }
        
        log.info("发现 {} 个失败的事务，开始自动恢复", failedTransactions.size());
        
        for (SagaTransaction transaction : failedTransactions) {
            try {
                recoverTransaction(transaction.getTransactionId());
            } catch (Exception e) {
                log.error("自动恢复事务失败，TransactionId: {}", 
                        transaction.getTransactionId(), e);
            }
        }
    }
    
    /**
     * 根据业务ID恢复事务
     * 
     * @param businessId 业务ID
     * @return 是否成功
     */
    public boolean recoverByBusinessId(String businessId) {
        List<SagaTransaction> transactions = sagaTransactionManager.getTransactionsByBusinessId(businessId);
        
        if (transactions.isEmpty()) {
            log.warn("未找到事务，BusinessId: {}", businessId);
            return false;
        }
        
        // 恢复最新的失败事务
        for (SagaTransaction transaction : transactions) {
            if (transaction.getStatus() == SagaTransaction.TransactionStatus.FAILED.getCode() ||
                transaction.getStatus() == SagaTransaction.TransactionStatus.COMPENSATING.getCode()) {
                return recoverTransaction(transaction.getTransactionId());
            }
        }
        
        return false;
    }
}
