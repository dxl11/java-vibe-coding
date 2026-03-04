package com.vibe.common.core.saga.alert;

import com.vibe.common.core.saga.config.SagaProperties;
import com.vibe.common.core.saga.entity.SagaTransaction;
import com.vibe.common.core.saga.mapper.SagaTransactionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SAGA 告警管理器
 * 监控异常情况并发送告警
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaAlertManager {
    
    @Autowired
    private SagaTransactionMapper sagaTransactionMapper;
    
    @Autowired
    private SagaProperties sagaProperties;
    
    /**
     * 上次告警时间（避免重复告警）
     */
    private LocalDateTime lastAlertTime = LocalDateTime.now();
    
    /**
     * 定期检查告警
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void checkAlerts() {
        if (!sagaProperties.getAlert().getEnabled()) {
            return;
        }
        
        try {
            // 检查事务失败率
            checkTransactionFailureRate();
            
            // 检查超时事务
            checkTimeoutTransactions();
            
            // 检查补偿失败
            checkCompensationFailures();
            
        } catch (Exception e) {
            log.error("检查告警异常", e);
        }
    }
    
    /**
     * 检查事务失败率
     */
    private void checkTransactionFailureRate() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        
        // 查询最近1分钟的事务
        List<SagaTransaction> recentTransactions = sagaTransactionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaTransaction>()
                        .ge(SagaTransaction::getCreateTime, oneMinuteAgo)
                        .eq(SagaTransaction::getIsDeleted, 0)
        );
        
        if (recentTransactions.isEmpty()) {
            return;
        }
        
        long totalCount = recentTransactions.size();
        long failedCount = recentTransactions.stream()
                .filter(t -> t.getStatus() == SagaTransaction.TransactionStatus.FAILED.getCode() ||
                           t.getStatus() == SagaTransaction.TransactionStatus.COMPENSATED.getCode())
                .count();
        
        double failureRate = (failedCount * 100.0) / totalCount;
        
        SagaProperties.AlertConfig alertConfig = sagaProperties.getAlert();
        if (failureRate >= alertConfig.getFailureRateThreshold()) {
            sendAlert("SAGA事务失败率告警", 
                    String.format("最近1分钟事务失败率: %.2f%%, 阈值: %.2f%%, 总事务数: %d, 失败数: %d", 
                            failureRate, alertConfig.getFailureRateThreshold(), totalCount, failedCount));
        }
    }
    
    /**
     * 检查超时事务
     */
    private void checkTimeoutTransactions() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        
        // 查询最近1分钟超时的事务
        List<SagaTransaction> timeoutTransactions = sagaTransactionMapper.selectTimeoutTransactions(currentTime, 1000);
        
        long recentTimeoutCount = timeoutTransactions.stream()
                .filter(t -> t.getCreateTime().isAfter(oneMinuteAgo))
                .count();
        
        SagaProperties.AlertConfig alertConfig = sagaProperties.getAlert();
        if (recentTimeoutCount >= alertConfig.getTimeoutTransactionThreshold()) {
            sendAlert("SAGA超时事务告警", 
                    String.format("最近1分钟超时事务数: %d, 阈值: %d", 
                            recentTimeoutCount, alertConfig.getTimeoutTransactionThreshold()));
        }
    }
    
    /**
     * 检查补偿失败
     */
    private void checkCompensationFailures() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        
        // 查询最近1分钟补偿中的事务（可能补偿失败）
        List<SagaTransaction> compensatingTransactions = sagaTransactionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SagaTransaction>()
                        .eq(SagaTransaction::getStatus, SagaTransaction.TransactionStatus.COMPENSATING.getCode())
                        .ge(SagaTransaction::getCreateTime, oneMinuteAgo)
                        .eq(SagaTransaction::getIsDeleted, 0)
        );
        
        SagaProperties.AlertConfig alertConfig = sagaProperties.getAlert();
        if (compensatingTransactions.size() >= alertConfig.getCompensationFailureThreshold()) {
            sendAlert("SAGA补偿失败告警", 
                    String.format("最近1分钟补偿中的事务数: %d, 阈值: %d", 
                            compensatingTransactions.size(), alertConfig.getCompensationFailureThreshold()));
        }
    }
    
    /**
     * 发送告警
     * 
     * @param title 告警标题
     * @param message 告警消息
     */
    private void sendAlert(String title, String message) {
        // 避免重复告警（1分钟内不重复）
        if (LocalDateTime.now().minusMinutes(1).isBefore(lastAlertTime)) {
            return;
        }
        
        lastAlertTime = LocalDateTime.now();
        
        log.error("【SAGA告警】{}: {}", title, message);
        
        // TODO: 集成告警渠道（邮件、短信、钉钉等）
        // 这里先记录日志，后续可以扩展
    }
}
