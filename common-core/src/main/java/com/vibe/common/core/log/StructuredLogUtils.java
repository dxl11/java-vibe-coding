package com.vibe.common.core.log;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 结构化日志工具类
 * 提供结构化字段输出，便于 ELK 系统采集和分析
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public final class StructuredLogUtils {
    
    /**
     * 业务日志 Logger
     */
    private static final Logger BUSINESS_LOGGER = LoggerFactory.getLogger("BUSINESS");
    
    /**
     * 审计日志 Logger
     */
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");
    
    /**
     * 私有构造函数，防止实例化
     */
    private StructuredLogUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 记录结构化业务日志
     * 自动包含 TraceId、UserId、IP 等上下文信息（通过 MDC）
     * 
     * @param operation 操作类型
     * @param message 日志消息
     * @param params 参数（会自动脱敏）
     */
    public static void businessLog(String operation, String message, Object... params) {
        String traceId = TraceIdUtils.getTraceIdOrNull();
        String userId = MDC.get("userId");
        String ip = MDC.get("ip");
        String maskedParams = maskParams(params);
        
        // 使用结构化格式，便于 ELK 解析
        BUSINESS_LOGGER.info("OPERATION:{}|MESSAGE:{}|PARAMS:{}|TRACE_ID:{}|USER_ID:{}|IP:{}", 
                operation, message, maskedParams, traceId, userId, ip);
    }
    
    /**
     * 记录结构化审计日志
     * 
     * @param operation 操作类型
     * @param resource 资源类型
     * @param resourceId 资源ID
     * @param action 操作动作（CREATE/UPDATE/DELETE/QUERY等）
     * @param result 操作结果（SUCCESS/FAILED）
     * @param remark 备注
     */
    public static void auditLog(String operation, String resource, String resourceId, 
                                String action, String result, String remark) {
        String traceId = TraceIdUtils.getTraceIdOrNull();
        String userId = MDC.get("userId");
        String ip = MDC.get("ip");
        
        // 使用结构化格式，便于 ELK 解析
        AUDIT_LOGGER.info("OPERATION:{}|RESOURCE:{}|RESOURCE_ID:{}|ACTION:{}|RESULT:{}|USER_ID:{}|IP:{}|TRACE_ID:{}|REMARK:{}", 
                operation, resource, resourceId, action, result, userId, ip, traceId, remark);
    }
    
    /**
     * 记录结构化错误日志（带异常信息）
     * 
     * @param logger 日志记录器
     * @param message 错误消息
     * @param throwable 异常对象
     */
    public static void errorLog(Logger logger, String message, Throwable throwable) {
        String traceId = TraceIdUtils.getTraceIdOrNull();
        String userId = MDC.get("userId");
        String ip = MDC.get("ip");
        
        // 使用结构化格式，便于 ELK 解析
        logger.error("ERROR|MESSAGE:{}|TRACE_ID:{}|USER_ID:{}|IP:{}", 
                message, traceId, userId, ip, throwable);
    }
    
    /**
     * 记录结构化警告日志
     * 
     * @param logger 日志记录器
     * @param message 警告消息
     * @param params 参数
     */
    public static void warnLog(Logger logger, String message, Object... params) {
        String traceId = TraceIdUtils.getTraceIdOrNull();
        String maskedParams = maskParams(params);
        
        logger.warn("WARN|MESSAGE:{}|PARAMS:{}|TRACE_ID:{}", message, maskedParams, traceId);
    }
    
    /**
     * 脱敏参数
     * 
     * @param params 参数数组
     * @return 脱敏后的参数字符串
     */
    private static String maskParams(Object... params) {
        if (params == null || params.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object param : params) {
            if (param != null) {
                sb.append(LogMaskUtils.maskObject(param)).append(",");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
