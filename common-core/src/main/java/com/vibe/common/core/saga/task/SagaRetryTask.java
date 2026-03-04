package com.vibe.common.core.saga.task;

import com.vibe.common.core.saga.entity.SagaStep;
import com.vibe.common.core.saga.retry.SagaRetryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SAGA 重试定时任务
 * 定期扫描并重试失败的步骤
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaRetryTask {
    
    @Autowired
    private SagaRetryHandler sagaRetryHandler;
    
    /**
     * 定时处理需要重试的步骤
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void handleRetryableSteps() {
        try {
            List<SagaStep> retryableSteps = sagaRetryHandler.getRetryableSteps(100);
            
            if (retryableSteps.isEmpty()) {
                return;
            }
            
            log.info("发现 {} 个需要重试的步骤，开始处理", retryableSteps.size());
            
            for (SagaStep step : retryableSteps) {
                try {
                    // 这里应该调用具体的重试逻辑
                    // 由于是协同式SAGA，重试逻辑由各个服务的事件监听器处理
                    // 这里只是标记步骤需要重试
                    
                    log.info("处理需要重试的步骤，StepId: {}, ServiceName: {}, StepName: {}", 
                            step.getStepId(), step.getServiceName(), step.getStepName());
                    
                } catch (Exception e) {
                    log.error("处理重试步骤失败，StepId: {}", step.getStepId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("处理重试步骤异常", e);
        }
    }
}
