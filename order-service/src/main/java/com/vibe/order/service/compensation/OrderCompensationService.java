package com.vibe.order.service.compensation;

import com.vibe.order.enums.OrderStatus;

/**
 * 订单补偿服务接口
 * 处理订单相关的补偿操作
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface OrderCompensationService {
    
    /**
     * 补偿订单创建失败
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param reason 失败原因
     */
    void compensateOrderCreation(Long orderId, String orderNo, String reason);
    
    /**
     * 补偿订单支付失败
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param reason 失败原因
     */
    void compensateOrderPayment(Long orderId, String orderNo, String reason);
    
    /**
     * 取消订单并补偿
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param reason 取消原因
     */
    void cancelOrder(Long orderId, String orderNo, String reason);
}
