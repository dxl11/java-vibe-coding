package com.vibe.common.core.saga.task;

import com.vibe.common.core.saga.SagaTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SAGA 超时处理定时任务
 * 定期扫描并处理超时的事务
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaTimeoutTask {
    
    @Autowired
    private SagaTimeoutHandler sagaTimeoutHandler;
    
    /**
     * 定时处理超时事务
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void handleTimeoutTransactions() {
        try {
            sagaTimeoutHandler.handleTimeoutTransactions(100);
        } catch (Exception e) {
            log.error("处理超时事务异常", e);
        }
    }
}
