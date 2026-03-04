package com.vibe.common.core.saga.retry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * SAGA 重试策略
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaRetryPolicy {
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
    
    /**
     * 初始重试间隔（毫秒）
     */
    private Long initialIntervalMs;
    
    /**
     * 最大重试间隔（毫秒）
     */
    private Long maxIntervalMs;
    
    /**
     * 重试间隔倍数（指数退避）
     */
    private Double multiplier;
    
    /**
     * 可重试的异常类型（全限定类名）
     */
    @Builder.Default
    private List<String> retryableExceptions = new ArrayList<>();
    
    /**
     * 默认重试策略
     */
    public static SagaRetryPolicy defaultPolicy() {
        return SagaRetryPolicy.builder()
                .maxRetryCount(3)
                .initialIntervalMs(1000L)
                .maxIntervalMs(60000L)
                .multiplier(2.0)
                .build();
    }
    
    /**
     * 计算下次重试间隔（指数退避）
     * 
     * @param retryCount 当前重试次数
     * @return 重试间隔（毫秒）
     */
    public long calculateNextInterval(int retryCount) {
        if (retryCount <= 0) {
            return initialIntervalMs;
        }
        
        long interval = (long) (initialIntervalMs * Math.pow(multiplier, retryCount - 1));
        return Math.min(interval, maxIntervalMs);
    }
    
    /**
     * 判断异常是否可重试
     * 
     * @param exception 异常
     * @return 是否可重试
     */
    public boolean isRetryable(Throwable exception) {
        if (exception == null) {
            return false;
        }
        
        // 如果没有配置可重试异常，默认所有异常都可重试
        if (retryableExceptions == null || retryableExceptions.isEmpty()) {
            return true;
        }
        
        String exceptionClassName = exception.getClass().getName();
        return retryableExceptions.contains(exceptionClassName);
    }
}
