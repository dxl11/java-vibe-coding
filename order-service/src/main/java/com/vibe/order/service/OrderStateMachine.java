package com.vibe.order.service;

import com.vibe.order.enums.OrderStatus;

/**
 * 订单状态机接口
 * 管理订单状态流转
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface OrderStateMachine {
    
    /**
     * 转换订单状态
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @param eventType 事件类型
     * @param eventData 事件数据
     * @return 是否转换成功
     */
    boolean transition(Long orderId, String orderNo, OrderStatus fromStatus, 
                       OrderStatus toStatus, String eventType, String eventData);
    
    /**
     * 转换订单状态（自动获取原状态）
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param toStatus 目标状态
     * @param eventType 事件类型
     * @param eventData 事件数据
     * @return 是否转换成功
     */
    boolean transition(Long orderId, String orderNo, OrderStatus toStatus, 
                       String eventType, String eventData);
    
    /**
     * 获取当前订单状态
     * 
     * @param orderId 订单ID
     * @return 订单状态
     */
    OrderStatus getCurrentStatus(Long orderId);
}
