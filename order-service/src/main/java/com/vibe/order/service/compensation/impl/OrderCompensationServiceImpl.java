package com.vibe.order.service.compensation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.event.EventPublisher;
import com.vibe.order.entity.OrderItem;
import com.vibe.order.enums.OrderStatus;
import com.vibe.order.event.OrderCancelledEvent;
import com.vibe.order.mapper.OrderItemMapper;
import com.vibe.order.service.OrderStateMachine;
import com.vibe.order.service.compensation.OrderCompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单补偿服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class OrderCompensationServiceImpl implements OrderCompensationService {
    
    @Autowired
    private OrderStateMachine orderStateMachine;
    
    @Autowired
    private EventPublisher eventPublisher;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void compensateOrderCreation(Long orderId, String orderNo, String reason) {
        log.info("补偿订单创建失败，OrderId: {}, OrderNo: {}, Reason: {}", orderId, orderNo, reason);
        
        // 更新订单状态为创建失败
        orderStateMachine.transition(orderId, orderNo, OrderStatus.CREATION_FAILED, 
                "ORDER_CREATION_COMPENSATION", "{\"reason\":\"" + reason + "\"}");
        
        // 发送订单取消事件，触发其他服务的补偿
        OrderCancelledEvent cancelledEvent = OrderCancelledEvent.builder()
                .orderId(orderId)
                .orderNo(orderNo)
                .reason(reason)
                .build();
        
        eventPublisher.publishOrderEvent("order-cancel", cancelledEvent);
        
        log.info("订单创建失败补偿完成，OrderId: {}, OrderNo: {}", orderId, orderNo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void compensateOrderPayment(Long orderId, String orderNo, String reason) {
        log.info("补偿订单支付失败，OrderId: {}, OrderNo: {}, Reason: {}", orderId, orderNo, reason);
        
        // 更新订单状态为支付失败
        orderStateMachine.transition(orderId, orderNo, OrderStatus.PAYMENT_FAILED, 
                "ORDER_PAYMENT_COMPENSATION", "{\"reason\":\"" + reason + "\"}");
        
        log.info("订单支付失败补偿完成，OrderId: {}, OrderNo: {}", orderId, orderNo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String orderNo, String reason) {
        log.info("取消订单，OrderId: {}, OrderNo: {}, Reason: {}", orderId, orderNo, reason);
        
        // 更新订单状态为已取消
        orderStateMachine.transition(orderId, orderNo, OrderStatus.CANCELLED, 
                "ORDER_CANCEL", "{\"reason\":\"" + reason + "\"}");
        
        // 查询订单明细（用于库存回滚）
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .eq(OrderItem::getIsDeleted, 0)
        );
        
        // 转换为事件明细
        List<OrderCancelledEvent.OrderItemInfo> eventItems = orderItems.stream()
                .map(item -> OrderCancelledEvent.OrderItemInfo.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        
        // 发送订单取消事件，触发其他服务的补偿
        OrderCancelledEvent cancelledEvent = OrderCancelledEvent.builder()
                .orderId(orderId)
                .orderNo(orderNo)
                .reason(reason)
                .items(eventItems)
                .build();
        
        // 设置父类字段
        cancelledEvent.setEventType("ORDER_CANCELLED");
        cancelledEvent.setSource("order-service");
        cancelledEvent.setEventTime(java.time.LocalDateTime.now());
        
        eventPublisher.publishOrderEvent("order-cancel", cancelledEvent);
        
        log.info("订单取消完成，OrderId: {}, OrderNo: {}", orderId, orderNo);
    }
}
