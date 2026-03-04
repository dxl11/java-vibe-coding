package com.vibe.inventory.listener;

import com.vibe.common.core.event.AbstractEventListener;
import com.vibe.common.core.event.EventPublisher;
import com.vibe.common.core.saga.SagaTransactionManager;
import com.vibe.inventory.dto.InventoryDeductDTO;
import com.vibe.inventory.event.InventoryDeductedEvent;
import com.vibe.inventory.event.InventoryDeductFailedEvent;
import com.vibe.inventory.service.InventoryService;
import com.vibe.order.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单创建事件监听器
 * 处理订单创建后的库存扣减
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class OrderCreatedListener extends AbstractEventListener<OrderCreatedEvent> {
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private EventPublisher eventPublisher;
    
    @Autowired
    private SagaTransactionManager sagaTransactionManager;
    
    @Override
    public String getEventType() {
        return "ORDER_CREATED";
    }
    
    @Override
    protected void doHandle(OrderCreatedEvent event) {
        log.info("收到订单创建事件，OrderId: {}, OrderNo: {}", event.getOrderId(), event.getOrderNo());
        
        String transactionId = event.getBusinessId();
        String stepId = null;
        
        try {
            // 创建 SAGA 步骤
            stepId = sagaTransactionManager.createStep(
                    transactionId, 
                    "inventory-service", 
                    "INVENTORY_DEDUCT", 
                    1, 
                    "{\"orderId\":" + event.getOrderId() + ",\"orderNo\":\"" + event.getOrderNo() + "\"}"
            );
            
            long startTime = System.currentTimeMillis();
            
            // 遍历订单明细，扣减库存
            for (OrderCreatedEvent.OrderItemInfo item : event.getItems()) {
                InventoryDeductDTO deductDTO = InventoryDeductDTO.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build();
                
                boolean success = inventoryService.deductStock(deductDTO);
                
                if (!success) {
                    throw new RuntimeException("库存扣减失败，ProductId: " + item.getProductId());
                }
                
                log.info("库存扣减成功，OrderId: {}, ProductId: {}, Quantity: {}", 
                        event.getOrderId(), item.getProductId(), item.getQuantity());
            }
            
            long durationMs = System.currentTimeMillis() - startTime;
            
            // 记录步骤执行成功
            sagaTransactionManager.recordStepSuccess(stepId, "SUCCESS", durationMs);
            
            // 所有商品库存扣减成功，发送成功事件
            InventoryDeductedEvent successEvent = InventoryDeductedEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNo(event.getOrderNo())
                    .build();
            
            eventPublisher.publishInventoryEvent("inventory-deducted", successEvent);
            
            log.info("订单库存扣减完成，OrderId: {}, OrderNo: {}", event.getOrderId(), event.getOrderNo());
            
        } catch (Exception e) {
            log.error("库存扣减异常，OrderId: {}, ProductId: {}", 
                    event.getOrderId(), e);
            
            // 记录步骤执行失败
            if (stepId != null) {
                sagaTransactionManager.recordStepFailure(stepId, e.getMessage());
            }
            
            // 标记事务失败
            if (transactionId != null) {
                sagaTransactionManager.markTransactionFailed(transactionId, "库存扣减失败: " + e.getMessage());
            }
            
            // 发送库存扣减失败事件
            InventoryDeductFailedEvent failedEvent = InventoryDeductFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNo(event.getOrderNo())
                    .reason("库存扣减异常: " + e.getMessage())
                    .build();
            
            eventPublisher.publishInventoryEvent("inventory-deduct-failed", failedEvent);
        }
    }
}
