package com.vibe.inventory.listener;

import com.vibe.common.core.event.AbstractEventListener;
import com.vibe.inventory.service.compensation.InventoryCompensationService;
import com.vibe.order.event.OrderCancelledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单取消事件监听器
 * 回滚库存
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class OrderCancelledListener extends AbstractEventListener<OrderCancelledEvent> {
    
    @Autowired
    private InventoryCompensationService inventoryCompensationService;
    
    @Override
    public String getEventType() {
        return "ORDER_CANCELLED";
    }
    
    @Override
    protected void doHandle(OrderCancelledEvent event) {
        log.info("收到订单取消事件，OrderId: {}, OrderNo: {}, Reason: {}", 
                event.getOrderId(), event.getOrderNo(), event.getReason());
        
        // 检查事件中是否包含订单明细
        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("订单取消事件中未包含订单明细，无法回滚库存，OrderId: {}, OrderNo: {}", 
                    event.getOrderId(), event.getOrderNo());
            return;
        }
        
        // 遍历订单明细，逐个回滚库存
        int successCount = 0;
        int failCount = 0;
        
        for (OrderCancelledEvent.OrderItemInfo item : event.getItems()) {
            try {
                inventoryCompensationService.rollbackInventory(
                        event.getOrderId(),
                        event.getOrderNo(),
                        item.getProductId(),
                        item.getQuantity()
                );
                successCount++;
                log.info("库存回滚成功，OrderId: {}, ProductId: {}, Quantity: {}", 
                        event.getOrderId(), item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                failCount++;
                log.error("库存回滚失败，OrderId: {}, ProductId: {}, Quantity: {}", 
                        event.getOrderId(), item.getProductId(), item.getQuantity(), e);
            }
        }
        
        log.info("订单取消库存回滚处理完成，OrderId: {}, OrderNo: {}, 成功: {}, 失败: {}", 
                event.getOrderId(), event.getOrderNo(), successCount, failCount);
    }
}
