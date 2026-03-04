package com.vibe.common.core.saga.task;

import com.vibe.common.core.saga.recovery.SagaRecoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SAGA 恢复定时任务
 * 定期自动恢复失败的事务
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaRecoveryTask {
    
    @Autowired
    private SagaRecoveryService sagaRecoveryService;
    
    /**
     * 定时自动恢复失败的事务
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void autoRecoverTransactions() {
        try {
            sagaRecoveryService.autoRecoverTransactions(100);
        } catch (Exception e) {
            log.error("自动恢复事务异常", e);
        }
    }
}
