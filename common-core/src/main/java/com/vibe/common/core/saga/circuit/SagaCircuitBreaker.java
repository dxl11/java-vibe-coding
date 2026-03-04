package com.vibe.common.core.saga.circuit;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.vibe.common.core.saga.config.SagaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SAGA 熔断器
 * 集成 Sentinel，防止服务雪崩
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class SagaCircuitBreaker {
    
    @Autowired
    private SagaProperties sagaProperties;
    
    /**
     * 服务调用统计（服务名 -> 统计信息）
     */
    private final ConcurrentHashMap<String, ServiceStats> serviceStatsMap = new ConcurrentHashMap<>();
    
    /**
     * 服务熔断状态（服务名 -> 是否熔断）
     */
    private final ConcurrentHashMap<String, CircuitState> circuitStates = new ConcurrentHashMap<>();
    
    /**
     * 执行步骤（带熔断保护）
     * 
     * @param serviceName 服务名称
     * @param stepName 步骤名称
     * @param executor 执行逻辑
     * @return 执行结果
     */
    @SentinelResource(value = "saga-step-execute", blockHandler = "handleBlock")
    public <T> T executeWithCircuitBreaker(String serviceName, String stepName, 
                                            java.util.function.Supplier<T> executor) {
        // 检查熔断状态
        if (isCircuitOpen(serviceName)) {
            log.warn("服务熔断中，拒绝执行，ServiceName: {}, StepName: {}", serviceName, stepName);
            throw new CircuitBreakerOpenException("服务熔断中: " + serviceName);
        }
        
        ServiceStats stats = getOrCreateStats(serviceName);
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行步骤
            T result = executor.get();
            
            // 记录成功
            stats.recordSuccess();
            onHalfOpen(serviceName);
            
            return result;
            
        } catch (Exception e) {
            // 记录失败
            stats.recordFailure();
            
            // 检查是否需要熔断
            checkAndOpenCircuit(serviceName, stats);
            
            throw e;
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            stats.recordDuration(duration);
        }
    }
    
    /**
     * Sentinel 阻塞处理
     */
    public <T> T handleBlock(String serviceName, String stepName, 
                            java.util.function.Supplier<T> executor, BlockException ex) {
        log.warn("Sentinel 限流，ServiceName: {}, StepName: {}", serviceName, stepName);
        throw new CircuitBreakerOpenException("Sentinel 限流: " + serviceName);
    }
    
    /**
     * 检查服务是否熔断
     * 
     * @param serviceName 服务名称
     * @return 是否熔断
     */
    public boolean isCircuitOpen(String serviceName) {
        CircuitState state = circuitStates.get(serviceName);
        if (state == null) {
            return false;
        }
        
        // 检查熔断时间窗口
        if (System.currentTimeMillis() - state.getOpenTime() > 
                sagaProperties.getCircuitBreaker().getTimeWindowSeconds() * 1000L) {
            // 熔断时间窗口已过，进入半开状态
            state.setHalfOpen(true);
            log.info("熔断时间窗口已过，进入半开状态，ServiceName: {}", serviceName);
        }
        
        return state.isOpen() && !state.isHalfOpen();
    }
    
    /**
     * 检查并打开熔断器
     * 
     * @param serviceName 服务名称
     * @param stats 统计信息
     */
    private void checkAndOpenCircuit(String serviceName, ServiceStats stats) {
        SagaProperties.CircuitBreakerConfig config = sagaProperties.getCircuitBreaker();
        
        if (!config.getEnabled()) {
            return;
        }
        
        // 计算失败率
        double failureRate = stats.getFailureRate();
        
        // 如果失败率超过阈值，且请求数达到最小要求，则打开熔断器
        if (stats.getTotalCalls() >= config.getMinimumNumberOfCalls() &&
            failureRate >= config.getFailureRateThreshold()) {
            
            CircuitState state = circuitStates.computeIfAbsent(serviceName, 
                    k -> new CircuitState());
            state.setOpen(true);
            state.setOpenTime(System.currentTimeMillis());
            state.setHalfOpen(false);
            
            log.error("打开熔断器，ServiceName: {}, FailureRate: {}%, TotalCalls: {}", 
                    serviceName, failureRate, stats.getTotalCalls());
        }
    }
    
    /**
     * 半开状态处理
     * 
     * @param serviceName 服务名称
     */
    private void onHalfOpen(String serviceName) {
        CircuitState state = circuitStates.get(serviceName);
        if (state != null && state.isHalfOpen()) {
            // 半开状态下成功，关闭熔断器
            state.setOpen(false);
            state.setHalfOpen(false);
            log.info("半开状态下成功，关闭熔断器，ServiceName: {}", serviceName);
        }
    }
    
    /**
     * 获取或创建服务统计信息
     * 
     * @param serviceName 服务名称
     * @return 统计信息
     */
    private ServiceStats getOrCreateStats(String serviceName) {
        return serviceStatsMap.computeIfAbsent(serviceName, k -> new ServiceStats());
    }
    
    /**
     * 服务统计信息
     */
    private static class ServiceStats {
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicInteger totalCalls = new AtomicInteger(0);
        
        public void recordSuccess() {
            successCount.incrementAndGet();
            totalCalls.incrementAndGet();
        }
        
        public void recordFailure() {
            failureCount.incrementAndGet();
            totalCalls.incrementAndGet();
        }
        
        public void recordDuration(long duration) {
            totalDuration.addAndGet(duration);
        }
        
        public double getFailureRate() {
            int total = totalCalls.get();
            if (total == 0) {
                return 0.0;
            }
            return (failureCount.get() * 100.0) / total;
        }
        
        public int getTotalCalls() {
            return totalCalls.get();
        }
        
        public long getAverageDuration() {
            int total = totalCalls.get();
            if (total == 0) {
                return 0;
            }
            return totalDuration.get() / total;
        }
    }
    
    /**
     * 熔断器状态
     */
    private static class CircuitState {
        private boolean open = false;
        private boolean halfOpen = false;
        private long openTime = 0;
        
        public boolean isOpen() {
            return open;
        }
        
        public void setOpen(boolean open) {
            this.open = open;
        }
        
        public boolean isHalfOpen() {
            return halfOpen;
        }
        
        public void setHalfOpen(boolean halfOpen) {
            this.halfOpen = halfOpen;
        }
        
        public long getOpenTime() {
            return openTime;
        }
        
        public void setOpenTime(long openTime) {
            this.openTime = openTime;
        }
    }
    
    /**
     * 熔断器打开异常
     */
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
