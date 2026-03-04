package com.vibe.order.listener;

import com.vibe.common.core.event.AbstractEventListener;
import com.vibe.inventory.event.InventoryDeductFailedEvent;
import com.vibe.order.enums.OrderStatus;
import com.vibe.order.service.OrderStateMachine;
import com.vibe.order.service.compensation.OrderCompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 库存扣减失败事件监听器
 * 触发订单补偿
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class InventoryDeductFailedListener extends AbstractEventListener<InventoryDeductFailedEvent> {
    
    @Autowired
    private OrderStateMachine orderStateMachine;
    
    @Autowired
    private OrderCompensationService orderCompensationService;
    
    @Override
    public String getEventType() {
        return "INVENTORY_DEDUCT_FAILED";
    }
    
    @Override
    protected void doHandle(InventoryDeductFailedEvent event) {
        log.warn("收到库存扣减失败事件，OrderId: {}, OrderNo: {}, Reason: {}", 
                event.getOrderId(), event.getOrderNo(), event.getReason());
        
        // 触发订单创建失败补偿
        orderCompensationService.compensateOrderCreation(
                event.getOrderId(), 
                event.getOrderNo(), 
                "库存扣减失败: " + event.getReason()
        );
        
        log.info("订单创建失败补偿完成，OrderId: {}, OrderNo: {}", event.getOrderId(), event.getOrderNo());
    }
}
