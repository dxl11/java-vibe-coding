package com.vibe.common.core.log.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.common.core.log.LogMaskUtils;
import com.vibe.common.core.log.PerformanceLogger;
import com.vibe.common.core.log.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求日志拦截器
 * 记录请求参数、响应结果、处理时间等
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final String START_TIME = "startTime";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);
        
        // 生成或获取 TraceId
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceIdUtils.generateTraceId();
        }
        TraceIdUtils.setTraceId(traceId);
        
        // 在响应头中添加 TraceId，便于前端追踪
        response.setHeader("X-Trace-Id", traceId);
        
        // 设置用户ID到 MDC
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            MDC.put("userId", userId);
        }
        
        // 设置IP地址到 MDC
        String ip = getClientIp(request);
        MDC.put("ip", ip);
        
        // 记录请求日志
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        Map<String, String> headers = getHeaders(request);
        
        log.info("REQUEST|METHOD:{}|URI:{}|QUERY:{}|HEADERS:{}|IP:{}|TRACE_ID:{}", 
                method, uri, queryString, maskHeaders(headers), ip, traceId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        if (startTime == null) {
            return;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        boolean success = status >= 200 && status < 400;
        
        // 记录响应日志
        if (ex != null) {
            log.error("RESPONSE|METHOD:{}|URI:{}|STATUS:{}|DURATION:{}ms|ERROR:{}|TRACE_ID:{}", 
                    method, uri, status, duration, ex.getMessage(), TraceIdUtils.getTraceIdOrNull(), ex);
        } else {
            log.info("RESPONSE|METHOD:{}|URI:{}|STATUS:{}|DURATION:{}ms|TRACE_ID:{}", 
                    method, uri, status, duration, TraceIdUtils.getTraceIdOrNull());
        }
        
        // 记录性能日志
        PerformanceLogger.logApiPerformance(method, uri, duration, success);
        
        // 清除 MDC
        TraceIdUtils.clear();
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 获取请求头
     * 
     * @param request HTTP请求
     * @return 请求头Map
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }
    
    /**
     * 脱敏请求头
     * 
     * @param headers 请求头Map
     * @return 脱敏后的请求头字符串
     */
    private String maskHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        Map<String, String> maskedHeaders = new HashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();
            // 脱敏敏感请求头
            if (key.contains("authorization") || key.contains("token") || key.contains("password")) {
                value = LogMaskUtils.maskPassword(value);
            }
            maskedHeaders.put(entry.getKey(), value);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(maskedHeaders);
        } catch (Exception e) {
            return maskedHeaders.toString();
        }
    }
}
