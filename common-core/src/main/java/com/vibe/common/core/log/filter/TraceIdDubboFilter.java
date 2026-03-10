package com.vibe.common.core.log.filter;

import com.vibe.common.core.log.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.MDC;

/**
 * Dubbo TraceId 过滤器
 * 自动传递 TraceId 和用户信息，确保分布式调用链路追踪
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Activate(group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER}, order = -10000)
public class TraceIdDubboFilter implements Filter {
    
    /**
     * TraceId 在 Dubbo Attachment 中的键名
     */
    private static final String TRACE_ID_KEY = "X-Trace-Id";
    
    /**
     * 用户ID 在 Dubbo Attachment 中的键名
     */
    private static final String USER_ID_KEY = "X-User-Id";
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 消费者端：传递 TraceId 和用户信息
        if (RpcContext.getContext().isConsumerSide()) {
            String traceId = TraceIdUtils.getTraceIdOrNull();
            if (traceId != null && !traceId.isEmpty()) {
                invocation.setAttachment(TRACE_ID_KEY, traceId);
            }
            
            String userId = MDC.get("userId");
            if (userId != null && !userId.isEmpty()) {
                invocation.setAttachment(USER_ID_KEY, userId);
            }
        }
        
        // 提供者端：接收 TraceId 和用户信息，设置到 MDC
        if (RpcContext.getContext().isProviderSide()) {
            String traceId = invocation.getAttachment(TRACE_ID_KEY);
            if (traceId != null && !traceId.isEmpty()) {
                TraceIdUtils.setTraceId(traceId);
            } else {
                // 如果没有 TraceId，生成新的
                TraceIdUtils.setTraceId(TraceIdUtils.generateTraceId());
            }
            
            String userId = invocation.getAttachment(USER_ID_KEY);
            if (userId != null && !userId.isEmpty()) {
                MDC.put("userId", userId);
            }
        }
        
        try {
            return invoker.invoke(invocation);
        } finally {
            // 提供者端：清理 MDC（避免线程池复用导致上下文污染）
            if (RpcContext.getContext().isProviderSide()) {
                TraceIdUtils.clear();
            }
        }
    }
}
