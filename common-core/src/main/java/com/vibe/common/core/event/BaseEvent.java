package com.vibe.common.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件基类
 * 所有领域事件都应继承此类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 事件ID（唯一标识）
     */
    private String eventId;
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 业务ID（如订单号）
     */
    private String businessId;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 链路追踪ID
     */
    private String traceId;
    
    /**
     * 事件来源服务
     */
    private String source;
    
    /**
     * 事件数据（JSON格式）
     */
    private String eventData;
}
