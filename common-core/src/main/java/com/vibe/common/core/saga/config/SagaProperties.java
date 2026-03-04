package com.vibe.common.core.saga.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SAGA 配置属性
 * 从配置文件中读取 SAGA 相关配置
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Component
@ConfigurationProperties(prefix = "vibe.saga")
public class SagaProperties {
    
    /**
     * 默认超时时间（秒）
     */
    private Integer defaultTimeoutSeconds = 300;
    
    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();
    
    /**
     * 熔断器配置
     */
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
    
    /**
     * 监控配置
     */
    private MetricsConfig metrics = new MetricsConfig();
    
    /**
     * 告警配置
     */
    private AlertConfig alert = new AlertConfig();
    
    /**
     * 恢复配置
     */
    private RecoveryConfig recovery = new RecoveryConfig();
    
    /**
     * 重试配置
     */
    @Data
    public static class RetryConfig {
        /**
         * 最大重试次数
         */
        private Integer maxRetryCount = 3;
        
        /**
         * 初始重试间隔（毫秒）
         */
        private Long initialIntervalMs = 1000L;
        
        /**
         * 最大重试间隔（毫秒）
         */
        private Long maxIntervalMs = 60000L;
        
        /**
         * 重试间隔倍数（指数退避）
         */
        private Double multiplier = 2.0;
        
        /**
         * 可重试的异常类型（全限定类名）
         */
        private List<String> retryableExceptions = new ArrayList<>();
    }
    
    /**
     * 熔断器配置
     */
    @Data
    public static class CircuitBreakerConfig {
        /**
         * 是否启用熔断器
         */
        private Boolean enabled = true;
        
        /**
         * 失败率阈值（百分比，0-100）
         */
        private Double failureRateThreshold = 50.0;
        
        /**
         * 最小请求数（达到此数量后才开始计算失败率）
         */
        private Integer minimumNumberOfCalls = 10;
        
        /**
         * 熔断时间窗口（秒）
         */
        private Integer timeWindowSeconds = 60;
        
        /**
         * 半开状态下的最大请求数
         */
        private Integer permittedNumberOfCallsInHalfOpenState = 3;
    }
    
    /**
     * 监控配置
     */
    @Data
    public static class MetricsConfig {
        /**
         * 是否启用监控
         */
        private Boolean enabled = true;
        
        /**
         * 指标收集间隔（秒）
         */
        private Integer collectIntervalSeconds = 60;
    }
    
    /**
     * 告警配置
     */
    @Data
    public static class AlertConfig {
        /**
         * 是否启用告警
         */
        private Boolean enabled = true;
        
        /**
         * 事务失败率告警阈值（百分比，0-100）
         */
        private Double failureRateThreshold = 10.0;
        
        /**
         * 超时事务告警阈值（每分钟）
         */
        private Integer timeoutTransactionThreshold = 10;
        
        /**
         * 补偿失败告警阈值（每分钟）
         */
        private Integer compensationFailureThreshold = 5;
    }
    
    /**
     * 恢复配置
     */
    @Data
    public static class RecoveryConfig {
        /**
         * 是否启用自动恢复
         */
        private Boolean autoRecoveryEnabled = true;
        
        /**
         * 自动恢复间隔（秒）
         */
        private Integer autoRecoveryIntervalSeconds = 300;
        
        /**
         * 最大恢复次数
         */
        private Integer maxRecoveryCount = 3;
    }
}
