package com.vibe.common.core.saga;

import com.vibe.common.core.saga.entity.SagaTransaction;
import com.vibe.common.core.saga.entity.SagaStep;
import com.vibe.common.core.saga.mapper.SagaTransactionMapper;
import com.vibe.common.core.saga.mapper.SagaStepMapper;
import com.vibe.common.core.saga.metrics.SagaMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * SAGA 事务管理器
 * 负责创建事务、记录步骤执行、触发补偿等
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaTransactionManager {
    
    @Autowired
    private SagaTransactionMapper sagaTransactionMapper;
    
    @Autowired
    private SagaStepMapper sagaStepMapper;
    
    @Autowired(required = false)
    private SagaMetrics sagaMetrics;
    
    /**
     * 创建 SAGA 事务
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param timeoutSeconds 超时时间（秒）
     * @return 事务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String createTransaction(String businessId, String businessType, Integer timeoutSeconds) {
        String transactionId = UUID.randomUUID().toString().replace("-", "");
        
        LocalDateTime expireTime = null;
        if (timeoutSeconds != null && timeoutSeconds > 0) {
            expireTime = LocalDateTime.now().plusSeconds(timeoutSeconds);
        }
        
        SagaTransaction transaction = SagaTransaction.builder()
                .transactionId(transactionId)
                .businessId(businessId)
                .businessType(businessType)
                .status(SagaTransaction.TransactionStatus.INIT.getCode())
                .timeoutSeconds(timeoutSeconds != null ? timeoutSeconds : 300)
                .expireTime(expireTime)
                .isDeleted(0)
                .build();
        
        sagaTransactionMapper.insert(transaction);
        
        // 记录指标
        if (sagaMetrics != null) {
            sagaMetrics.recordTransactionCreated(businessType);
        }
        
        log.info("创建 SAGA 事务，TransactionId: {}, BusinessId: {}, BusinessType: {}", 
                transactionId, businessId, businessType);
        
        return transactionId;
    }
    
    /**
     * 创建 SAGA 步骤
     * 
     * @param transactionId 事务ID
     * @param serviceName 服务名称
     * @param stepName 步骤名称
     * @param stepOrder 步骤顺序
     * @param requestData 请求数据
     * @return 步骤ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String createStep(String transactionId, String serviceName, String stepName, 
                            Integer stepOrder, String requestData) {
        String stepId = UUID.randomUUID().toString().replace("-", "");
        
        SagaStep step = SagaStep.builder()
                .stepId(stepId)
                .transactionId(transactionId)
                .serviceName(serviceName)
                .stepName(stepName)
                .stepOrder(stepOrder)
                .status(SagaStep.StepStatus.PENDING.getCode())
                .requestData(requestData)
                .retryCount(0)
                .maxRetryCount(3)
                .isDeleted(0)
                .build();
        
        sagaStepMapper.insert(step);
        
        // 更新事务当前步骤
        sagaTransactionMapper.updateCurrentStep(transactionId, stepName);
        
        log.info("创建 SAGA 步骤，StepId: {}, TransactionId: {}, ServiceName: {}, StepName: {}", 
                stepId, transactionId, serviceName, stepName);
        
        return stepId;
    }
    
    /**
     * 记录步骤执行成功
     * 
     * @param stepId 步骤ID
     * @param responseData 响应数据
     * @param durationMs 执行耗时（毫秒）
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordStepSuccess(String stepId, String responseData, Long durationMs) {
        LocalDateTime executeTime = LocalDateTime.now();
        
        sagaStepMapper.updateStatus(stepId, SagaStep.StepStatus.SUCCESS.getCode());
        sagaStepMapper.updateExecuteTime(stepId, executeTime, durationMs);
        
        // 更新步骤响应数据
        SagaStep step = sagaStepMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaStep>()
                        .eq(SagaStep::getStepId, stepId)
                        .eq(SagaStep::getIsDeleted, 0)
        );
        if (step != null) {
            step.setResponseData(responseData);
            sagaStepMapper.updateById(step);
        }
        
        log.info("记录步骤执行成功，StepId: {}, Duration: {}ms", stepId, durationMs);
    }
    
    /**
     * 记录步骤执行失败
     * 
     * @param stepId 步骤ID
     * @param errorMessage 错误信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordStepFailure(String stepId, String errorMessage) {
        LocalDateTime executeTime = LocalDateTime.now();
        
        sagaStepMapper.updateStatus(stepId, SagaStep.StepStatus.FAILED.getCode());
        sagaStepMapper.updateExecuteTime(stepId, executeTime, null);
        
        // 更新步骤错误信息
        SagaStep step = sagaStepMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaStep>()
                        .eq(SagaStep::getStepId, stepId)
                        .eq(SagaStep::getIsDeleted, 0)
        );
        if (step != null) {
            step.setErrorMessage(errorMessage);
            sagaStepMapper.updateById(step);
        }
        
        log.warn("记录步骤执行失败，StepId: {}, Error: {}", stepId, errorMessage);
    }
    
    /**
     * 记录步骤补偿
     * 
     * @param stepId 步骤ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordStepCompensation(String stepId) {
        LocalDateTime compensateTime = LocalDateTime.now();
        
        sagaStepMapper.updateStatus(stepId, SagaStep.StepStatus.COMPENSATED.getCode());
        sagaStepMapper.updateCompensateTime(stepId, compensateTime);
        
        // 记录指标
        if (sagaMetrics != null) {
            SagaStep step = sagaStepMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaStep>()
                            .eq(SagaStep::getStepId, stepId)
                            .eq(SagaStep::getIsDeleted, 0)
            );
            if (step != null) {
                sagaMetrics.recordCompensation(step.getServiceName(), step.getStepName());
            }
        }
        
        log.info("记录步骤补偿，StepId: {}", stepId);
    }
    
    /**
     * 更新事务状态
     * 
     * @param transactionId 事务ID
     * @param status 新状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTransactionStatus(String transactionId, SagaTransaction.TransactionStatus status) {
        sagaTransactionMapper.updateStatus(transactionId, status.getCode());
        
        // 记录指标
        if (sagaMetrics != null && (status == SagaTransaction.TransactionStatus.COMPLETED ||
                status == SagaTransaction.TransactionStatus.FAILED ||
                status == SagaTransaction.TransactionStatus.COMPENSATED)) {
            SagaTransaction transaction = getTransaction(transactionId);
            if (transaction != null) {
                boolean success = status == SagaTransaction.TransactionStatus.COMPLETED;
                long durationMs = 0;  // TODO: 计算实际耗时
                sagaMetrics.recordTransactionCompleted(transaction.getBusinessType(), success, durationMs);
            }
        }
        
        log.info("更新事务状态，TransactionId: {}, Status: {}", transactionId, status);
    }
    
    /**
     * 开始补偿流程
     * 
     * @param transactionId 事务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void startCompensation(String transactionId) {
        updateTransactionStatus(transactionId, SagaTransaction.TransactionStatus.COMPENSATING);
        log.info("开始补偿流程，TransactionId: {}", transactionId);
    }
    
    /**
     * 完成补偿流程
     * 
     * @param transactionId 事务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeCompensation(String transactionId) {
        updateTransactionStatus(transactionId, SagaTransaction.TransactionStatus.COMPENSATED);
        log.info("完成补偿流程，TransactionId: {}", transactionId);
    }
    
    /**
     * 标记事务失败
     * 
     * @param transactionId 事务ID
     * @param errorMessage 错误信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void markTransactionFailed(String transactionId, String errorMessage) {
        updateTransactionStatus(transactionId, SagaTransaction.TransactionStatus.FAILED);
        
        SagaTransaction transaction = sagaTransactionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaTransaction>()
                        .eq(SagaTransaction::getTransactionId, transactionId)
                        .eq(SagaTransaction::getIsDeleted, 0)
        );
        if (transaction != null) {
            transaction.setErrorMessage(errorMessage);
            sagaTransactionMapper.updateById(transaction);
        }
        
        log.error("标记事务失败，TransactionId: {}, Error: {}", transactionId, errorMessage);
    }
    
    /**
     * 根据事务ID查询事务
     * 
     * @param transactionId 事务ID
     * @return 事务
     */
    public SagaTransaction getTransaction(String transactionId) {
        return sagaTransactionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaTransaction>()
                        .eq(SagaTransaction::getTransactionId, transactionId)
                        .eq(SagaTransaction::getIsDeleted, 0)
        );
    }
    
    /**
     * 根据事务ID查询步骤列表
     * 
     * @param transactionId 事务ID
     * @return 步骤列表
     */
    public List<SagaStep> getSteps(String transactionId) {
        return sagaStepMapper.selectByTransactionId(transactionId);
    }
    
    /**
     * 根据业务ID查询事务
     * 
     * @param businessId 业务ID
     * @return 事务列表
     */
    public List<SagaTransaction> getTransactionsByBusinessId(String businessId) {
        return sagaTransactionMapper.selectByBusinessId(businessId);
    }
}
