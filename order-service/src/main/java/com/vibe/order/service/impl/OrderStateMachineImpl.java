    package com.vibe.order.service.impl;

import com.vibe.common.core.exception.BusinessException;
import com.vibe.order.entity.Order;
import com.vibe.order.entity.OrderStateLog;
import com.vibe.order.enums.OrderStatus;
import com.vibe.order.mapper.OrderMapper;
import com.vibe.order.mapper.OrderStateLogMapper;
import com.vibe.order.service.OrderStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 订单状态机实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class OrderStateMachineImpl implements OrderStateMachine {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderStateLogMapper orderStateLogMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transition(Long orderId, String orderNo, OrderStatus fromStatus, 
                             OrderStatus toStatus, String eventType, String eventData) {
        log.info("订单状态转换，OrderId: {}, OrderNo: {}, FromStatus: {}, ToStatus: {}, EventType: {}", 
                orderId, orderNo, fromStatus, toStatus, eventType);
        
        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(404, "订单不存在");
        }
        
        // 保存当前版本号，用于乐观锁
        Integer currentVersion = order.getVersion();
        
        // 验证原状态
        OrderStatus currentStatus = OrderStatus.getByCode(order.getStatus());
        if (fromStatus != null && currentStatus != fromStatus) {
            log.warn("订单状态不匹配，OrderId: {}, 期望状态: {}, 实际状态: {}", 
                    orderId, fromStatus, currentStatus);
            return false;
        }
        
        // 验证状态转换是否合法
        if (!currentStatus.canTransitionTo(toStatus)) {
            log.warn("订单状态转换不合法，OrderId: {}, FromStatus: {}, ToStatus: {}", 
                    orderId, currentStatus, toStatus);
            throw new BusinessException(400, "订单状态转换不合法: " + currentStatus + " -> " + toStatus);
        }
        
        // 使用乐观锁更新订单状态
        int updateRows = orderMapper.updateStatusWithVersion(orderId, toStatus.getCode(), currentVersion);
        if (updateRows == 0) {
            log.warn("订单状态更新失败，可能存在并发修改，OrderId: {}, CurrentStatus: {}, ToStatus: {}", 
                    orderId, currentStatus, toStatus);
            throw new BusinessException(409, "订单状态已变更，请重试");
        }
        
        // 记录状态流转日志
        OrderStateLog stateLog = OrderStateLog.builder()
                .orderId(orderId)
                .orderNo(orderNo)
                .fromState(fromStatus != null ? fromStatus.getCode() : currentStatus.getCode())
                .toState(toStatus.getCode())
                .eventType(eventType)
                .eventData(eventData)
                .createTime(LocalDateTime.now())
                .build();
        orderStateLogMapper.insert(stateLog);
        
        log.info("订单状态转换成功，OrderId: {}, OrderNo: {}, FromStatus: {}, ToStatus: {}", 
                orderId, orderNo, currentStatus, toStatus);
        
        return true;
    }
    
    @Override
    public boolean transition(Long orderId, String orderNo, OrderStatus toStatus, 
                             String eventType, String eventData) {
        // 查询当前状态
        OrderStatus currentStatus = getCurrentStatus(orderId);
        return transition(orderId, orderNo, currentStatus, toStatus, eventType, eventData);
    }
    
    @Override
    public OrderStatus getCurrentStatus(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            return null;
        }
        return OrderStatus.getByCode(order.getStatus());
    }
}
