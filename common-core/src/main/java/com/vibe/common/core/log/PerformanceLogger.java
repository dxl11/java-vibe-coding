package com.vibe.common.core.log;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 性能日志记录器
 * 用于记录接口性能、数据库查询性能等
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public final class PerformanceLogger {
    
    /**
     * 性能日志 Logger
     */
    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("PERFORMANCE");
    
    /**
     * 慢查询阈值（毫秒）
     */
    private static final long SLOW_QUERY_THRESHOLD = 1000;
    
    /**
     * 慢接口阈值（毫秒）
     */
    private static final long SLOW_API_THRESHOLD = 2000;
    
    /**
     * 私有构造函数，防止实例化
     */
    private PerformanceLogger() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 记录接口性能
     * 
     * @param method 方法名
     * @param uri 请求URI
     * @param duration 耗时（毫秒）
     * @param success 是否成功
     */
    public static void logApiPerformance(String method, String uri, long duration, boolean success) {
        String traceId = TraceIdUtils.getTraceIdOrNull();
        String level = duration > SLOW_API_THRESHOLD ? "SLOW" : "NORMAL";
        
        PERFORMANCE_LOGGER.info("API|{}|{}|{}|{}|{}|{}", 
                method, uri, duration, success ? "SUCCESS" : "FAILED", level, traceId);
        
        if (duration > SLOW_API_THRESHOLD) {
            log.warn("慢接口检测，URI: {}, 耗时: {}ms", uri, duration);
        }
    }
    
    /**
     * 记录数据库查询性能
     * 
     * @param sql SQL语句（脱敏后）
     * @param duration 耗时（毫秒）
     */
    public static void logDbPerformance(String sql, long duration) {
        String traceId = TraceIdUtils.getTraceIdOrNull();
        String level = duration > SLOW_QUERY_THRESHOLD ? "SLOW" : "NORMAL";
        
        PERFORMANCE_LOGGER.info("DB|{}|{}|{}", sql, duration, level);
        
        if (duration > SLOW_QUERY_THRESHOLD) {
            log.warn("慢查询检测，SQL: {}, 耗时: {}ms, TraceId: {}", sql, duration, traceId);
        }
    }
    
    /**
     * 记录外部服务调用性能
     * 
     * @param serviceName 服务名称
     * @param method 方法名
     * @param duration 耗时（毫秒）
     * @param success 是否成功
     */
    public static void logRpcPerformance(String serviceName, String method, long duration, boolean success) {
        String traceId = TraceIdUtils.getTraceIdOrNull();
        
        PERFORMANCE_LOGGER.info("RPC|{}|{}|{}|{}|{}", 
                serviceName, method, duration, success ? "SUCCESS" : "FAILED", traceId);
    }
    
    /**
     * 记录消息队列性能
     * 
     * @param topic Topic名称
     * @param tag Tag名称
     * @param operation 操作类型（SEND/RECEIVE）
     * @param duration 耗时（毫秒）
     * @param success 是否成功
     */
    public static void logMqPerformance(String topic, String tag, String operation, long duration, boolean success) {
        String traceId = TraceIdUtils.getTraceIdOrNull();
        
        PERFORMANCE_LOGGER.info("MQ|{}|{}|{}|{}|{}|{}", 
                topic, tag, operation, duration, success ? "SUCCESS" : "FAILED", traceId);
    }
    
    /**
     * 记录方法执行性能
     * 
     * @param className 类名
     * @param methodName 方法名
     * @param operation 操作名称
     * @param duration 耗时（毫秒）
     * @param threshold 阈值（毫秒）
     * @param success 是否成功
     */
    public static void logMethodPerformance(String className, String methodName, String operation, 
                                            long duration, long threshold, boolean success) {
        String traceId = TraceIdUtils.getTraceIdOrNull();
        String level = threshold > 0 && duration > threshold ? "SLOW" : "NORMAL";
        
        PERFORMANCE_LOGGER.info("METHOD|{}|{}|{}|{}|{}|{}|{}|{}", 
                className, methodName, operation, duration, threshold, level, 
                success ? "SUCCESS" : "FAILED", traceId);
        
        // 如果超过阈值，记录警告日志
        if (threshold > 0 && duration > threshold) {
            log.warn("方法执行超时，类: {}, 方法: {}, 操作: {}, 耗时: {}ms, 阈值: {}ms, TraceId: {}", 
                    className, methodName, operation, duration, threshold, traceId);
        }
    }
}
