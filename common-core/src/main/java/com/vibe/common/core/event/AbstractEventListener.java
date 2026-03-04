package com.vibe.common.core.event;

import com.vibe.common.core.log.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 事件监听器抽象类
 * 提供通用的事件处理逻辑
 * 
 * @param <T> 事件类型
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public abstract class AbstractEventListener<T extends BaseEvent> implements EventListener<T> {
    
    @Override
    public void onEvent(T event) {
        // 设置 TraceId
        if (event.getTraceId() != null && !event.getTraceId().isEmpty()) {
            TraceIdUtils.setTraceId(event.getTraceId());
        } else {
            TraceIdUtils.setTraceId(TraceIdUtils.generateTraceId());
        }
        
        try {
            log.info("开始处理事件，EventId: {}, EventType: {}, BusinessId: {}", 
                    event.getEventId(), event.getEventType(), event.getBusinessId());
            
            // 执行业务处理
            doHandle(event);
            
            log.info("事件处理成功，EventId: {}, EventType: {}", 
                    event.getEventId(), event.getEventType());
            
        } catch (Exception e) {
            log.error("事件处理失败，EventId: {}, EventType: {}", 
                    event.getEventId(), event.getEventType(), e);
            handleException(event, e);
        } finally {
            // 清除 TraceId
            TraceIdUtils.clearTraceId();
        }
    }
    
    /**
     * 执行业务处理
     * 子类需要实现此方法
     * 
     * @param event 事件对象
     */
    protected abstract void doHandle(T event);
    
    /**
     * 处理异常
     * 子类可以重写此方法实现自定义异常处理
     * 
     * @param event 事件对象
     * @param e 异常
     */
    protected void handleException(T event, Exception e) {
        // 默认实现：记录日志
        log.error("事件处理异常，EventId: {}", event.getEventId(), e);
    }
}
