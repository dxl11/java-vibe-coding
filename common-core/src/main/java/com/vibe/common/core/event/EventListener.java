package com.vibe.common.core.event;

/**
 * 事件监听器接口
 * 所有事件监听器都应实现此接口
 * 
 * @param <T> 事件类型
 * @author vibe
 * @date 2024-01-13
 */
public interface EventListener<T extends BaseEvent> {
    
    /**
     * 处理事件
     * 
     * @param event 事件对象
     */
    void onEvent(T event);
    
    /**
     * 获取支持的事件类型
     * 
     * @return 事件类型
     */
    String getEventType();
}
