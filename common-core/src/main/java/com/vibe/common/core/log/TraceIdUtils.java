package com.vibe.common.core.log;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 链路追踪ID工具类
 * 用于生成和传递 TraceId
 * 
 * @author vibe
 * @date 2024-01-13
 */
public final class TraceIdUtils {
    
    /**
     * TraceId 在 MDC 中的键名
     */
    private static final String TRACE_ID_KEY = "traceId";
    
    /**
     * 私有构造函数，防止实例化
     */
    private TraceIdUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 生成 TraceId
     * 
     * @return TraceId
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 设置 TraceId 到 MDC
     * 
     * @param traceId TraceId
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }
    
    /**
     * 获取当前 TraceId
     * 
     * @return TraceId，如果不存在则生成新的
     */
    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
            setTraceId(traceId);
        }
        return traceId;
    }
    
    /**
     * 获取当前 TraceId（不自动生成）
     * 
     * @return TraceId，如果不存在则返回 null
     */
    public static String getTraceIdOrNull() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    /**
     * 清除 TraceId
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }
    
    /**
     * 清除所有 MDC 数据
     */
    public static void clear() {
        MDC.clear();
    }
}
