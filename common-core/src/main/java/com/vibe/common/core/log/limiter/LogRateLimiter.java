package com.vibe.common.core.log.limiter;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 日志限流器
 * 使用令牌桶算法限制日志输出速率，避免高并发场景下日志过多
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public class LogRateLimiter {
    
    /**
     * 默认限流速率：每秒 10000 条日志
     */
    private static final long DEFAULT_RATE = 10000L;
    
    /**
     * 令牌桶容量：允许突发 20000 条日志
     */
    private static final long DEFAULT_CAPACITY = 20000L;
    
    /**
     * 当前令牌数
     */
    private final AtomicLong tokens = new AtomicLong(DEFAULT_CAPACITY);
    
    /**
     * 每秒生成的令牌数（速率）
     */
    private volatile long rate = DEFAULT_RATE;
    
    /**
     * 令牌桶容量
     */
    private volatile long capacity = DEFAULT_CAPACITY;
    
    /**
     * 上次更新时间（毫秒）
     */
    private volatile long lastUpdateTime = System.currentTimeMillis();
    
    /**
     * 更新锁
     */
    private final ReentrantLock updateLock = new ReentrantLock();
    
    /**
     * 被限流的日志计数
     */
    private final AtomicLong droppedCount = new AtomicLong(0);
    
    /**
     * 私有构造函数，使用单例模式
     */
    private LogRateLimiter() {
    }
    
    /**
     * 单例实例
     */
    private static final LogRateLimiter INSTANCE = new LogRateLimiter();
    
    /**
     * 获取单例实例
     * 
     * @return LogRateLimiter 实例
     */
    public static LogRateLimiter getInstance() {
        return INSTANCE;
    }
    
    /**
     * 尝试获取令牌
     * 
     * @return true 表示允许记录日志，false 表示被限流
     */
    public boolean tryAcquire() {
        updateTokens();
        
        long currentTokens = tokens.get();
        if (currentTokens > 0) {
            // 有令牌，消耗一个
            if (tokens.compareAndSet(currentTokens, currentTokens - 1)) {
                return true;
            }
        }
        
        // 无令牌，被限流
        long dropped = droppedCount.incrementAndGet();
        // 每 1000 条被限流的日志记录一次警告
        if (dropped % 1000 == 0) {
            log.warn("日志限流：已丢弃 {} 条日志，当前速率: {}/s", dropped, rate);
        }
        return false;
    }
    
    /**
     * 更新令牌数
     */
    private void updateTokens() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastUpdateTime;
        
        if (elapsed < 100) {
            // 小于 100ms 不更新，避免频繁计算
            return;
        }
        
        if (updateLock.tryLock()) {
            try {
                // 计算应该生成的令牌数
                long tokensToAdd = (elapsed * rate) / 1000L;
                if (tokensToAdd > 0) {
                    long currentTokens = tokens.get();
                    long newTokens = Math.min(capacity, currentTokens + tokensToAdd);
                    tokens.set(newTokens);
                    lastUpdateTime = now;
                }
            } finally {
                updateLock.unlock();
            }
        }
    }
    
    /**
     * 设置限流速率
     * 
     * @param rate 每秒允许的日志条数
     */
    public void setRate(long rate) {
        if (rate < 0) {
            log.warn("限流速率不能为负数，当前值: {}", rate);
            return;
        }
        this.rate = rate;
        log.info("更新日志限流速率: {}/s", rate);
    }
    
    /**
     * 设置令牌桶容量
     * 
     * @param capacity 令牌桶容量
     */
    public void setCapacity(long capacity) {
        if (capacity < 0) {
            log.warn("令牌桶容量不能为负数，当前值: {}", capacity);
            return;
        }
        this.capacity = capacity;
        // 调整当前令牌数，不超过新容量
        long currentTokens = tokens.get();
        if (currentTokens > capacity) {
            tokens.set(capacity);
        }
        log.info("更新日志限流容量: {}", capacity);
    }
    
    /**
     * 获取当前限流速率
     * 
     * @return 速率
     */
    public long getRate() {
        return rate;
    }
    
    /**
     * 获取当前令牌数
     * 
     * @return 令牌数
     */
    public long getCurrentTokens() {
        updateTokens();
        return tokens.get();
    }
    
    /**
     * 获取被限流的日志总数
     * 
     * @return 被限流的日志数
     */
    public long getDroppedCount() {
        return droppedCount.get();
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        droppedCount.set(0);
    }
}
