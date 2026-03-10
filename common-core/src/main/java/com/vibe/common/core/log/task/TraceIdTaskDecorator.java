package com.vibe.common.core.log.task;

import com.vibe.common.core.log.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;

/**
 * 定时任务 TraceId 装饰器
 * 为定时任务自动设置 TraceId，确保日志可追踪
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public class TraceIdTaskDecorator implements TaskDecorator {
    
    @Override
    public Runnable decorate(Runnable runnable) {
        // 获取当前线程的 TraceId（如果有）
        String parentTraceId = TraceIdUtils.getTraceIdOrNull();
        
        return () -> {
            try {
                // 设置 TraceId（继承父线程或生成新的）
                if (parentTraceId != null && !parentTraceId.isEmpty()) {
                    TraceIdUtils.setTraceId(parentTraceId);
                } else {
                    TraceIdUtils.setTraceId(TraceIdUtils.generateTraceId());
                }
                
                // 执行任务
                runnable.run();
            } finally {
                // 清理 TraceId，避免线程池复用导致上下文污染
                TraceIdUtils.clear();
            }
        };
    }
}
