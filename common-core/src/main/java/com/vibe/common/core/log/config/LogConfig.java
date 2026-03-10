package com.vibe.common.core.log.config;

import com.vibe.common.core.log.limiter.LogRateLimiter;
import com.vibe.common.core.log.sampler.LogSampler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.annotation.PostConstruct;

/**
 * 日志配置类
 * 支持从配置文件动态调整日志采样率和限流参数
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Configuration
public class LogConfig {
    
    /**
     * DEBUG 级别日志采样率（0.0-1.0）
     */
    @Value("${logging.sampler.debug-rate:0.1}")
    private double debugSampleRate;
    
    /**
     * TRACE 级别日志采样率（0.0-1.0）
     */
    @Value("${logging.sampler.trace-rate:0.01}")
    private double traceSampleRate;
    
    /**
     * 日志限流速率（每秒允许的日志条数）
     */
    @Value("${logging.limiter.rate:10000}")
    private long limiterRate;
    
    /**
     * 日志限流容量（令牌桶容量）
     */
    @Value("${logging.limiter.capacity:20000}")
    private long limiterCapacity;
    
    /**
     * 是否启用日志采样
     */
    @Value("${logging.sampler.enabled:true}")
    private boolean samplerEnabled;
    
    /**
     * 是否启用日志限流
     */
    @Value("${logging.limiter.enabled:true}")
    private boolean limiterEnabled;
    
    @PostConstruct
    public void init() {
        // 初始化采样器配置
        if (samplerEnabled) {
            LogSampler sampler = LogSampler.getInstance();
            sampler.setSampleRate(Level.DEBUG, debugSampleRate);
            sampler.setSampleRate(Level.TRACE, traceSampleRate);
            log.info("日志采样器已启用，DEBUG采样率: {}, TRACE采样率: {}", 
                    debugSampleRate, traceSampleRate);
        } else {
            log.info("日志采样器已禁用");
        }
        
        // 初始化限流器配置
        if (limiterEnabled) {
            LogRateLimiter limiter = LogRateLimiter.getInstance();
            limiter.setRate(limiterRate);
            limiter.setCapacity(limiterCapacity);
            log.info("日志限流器已启用，速率: {}/s, 容量: {}", limiterRate, limiterCapacity);
        } else {
            log.info("日志限流器已禁用");
        }
    }
    
    /**
     * 应用启动完成后，输出日志配置信息
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("日志系统配置完成 - 采样器: {}, 限流器: {}", 
                samplerEnabled ? "启用" : "禁用", 
                limiterEnabled ? "启用" : "禁用");
    }
}
