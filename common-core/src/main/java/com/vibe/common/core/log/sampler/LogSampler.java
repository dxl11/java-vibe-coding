package com.vibe.common.core.log.sampler;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 日志采样器
 * 根据日志级别和配置的采样率决定是否记录日志
 * 用于高并发场景下减少日志量，避免影响性能和 ELK 存储
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public class LogSampler {
    
    /**
     * 默认采样率配置
     * key: 日志级别，value: 采样率（0.0-1.0，1.0 表示 100% 采样）
     */
    private static final Map<Level, Double> DEFAULT_SAMPLE_RATES = new ConcurrentHashMap<>();
    
    static {
        // 默认采样率：ERROR 和 WARN 100% 采样，INFO 100% 采样，DEBUG 10% 采样
        DEFAULT_SAMPLE_RATES.put(Level.ERROR, 1.0);
        DEFAULT_SAMPLE_RATES.put(Level.WARN, 1.0);
        DEFAULT_SAMPLE_RATES.put(Level.INFO, 1.0);
        DEFAULT_SAMPLE_RATES.put(Level.DEBUG, 0.1);
        DEFAULT_SAMPLE_RATES.put(Level.TRACE, 0.01);
    }
    
    /**
     * 当前采样率配置（支持动态更新）
     */
    private final Map<Level, Double> sampleRates = new ConcurrentHashMap<>(DEFAULT_SAMPLE_RATES);
    
    /**
     * 私有构造函数，使用单例模式
     */
    private LogSampler() {
    }
    
    /**
     * 单例实例
     */
    private static final LogSampler INSTANCE = new LogSampler();
    
    /**
     * 获取单例实例
     * 
     * @return LogSampler 实例
     */
    public static LogSampler getInstance() {
        return INSTANCE;
    }
    
    /**
     * 判断是否应该采样
     * 
     * @param level 日志级别
     * @return true 表示应该记录日志，false 表示跳过
     */
    public boolean shouldSample(Level level) {
        Double rate = sampleRates.get(level);
        if (rate == null) {
            // 未知级别，默认 100% 采样
            return true;
        }
        
        if (rate >= 1.0) {
            return true;
        }
        
        if (rate <= 0.0) {
            return false;
        }
        
        // 随机采样
        return ThreadLocalRandom.current().nextDouble() < rate;
    }
    
    /**
     * 设置采样率
     * 
     * @param level 日志级别
     * @param rate 采样率（0.0-1.0）
     */
    public void setSampleRate(Level level, double rate) {
        if (rate < 0.0 || rate > 1.0) {
            log.warn("采样率必须在 0.0-1.0 之间，当前值: {}", rate);
            return;
        }
        sampleRates.put(level, rate);
        log.info("更新日志采样率，Level: {}, Rate: {}", level, rate);
    }
    
    /**
     * 获取采样率
     * 
     * @param level 日志级别
     * @return 采样率
     */
    public double getSampleRate(Level level) {
        return sampleRates.getOrDefault(level, 1.0);
    }
    
    /**
     * 重置为默认采样率
     */
    public void resetToDefault() {
        sampleRates.clear();
        sampleRates.putAll(DEFAULT_SAMPLE_RATES);
        log.info("日志采样率已重置为默认值");
    }
}
