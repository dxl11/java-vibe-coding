package com.vibe.order.listener;

import com.vibe.common.core.event.AbstractEventListener;
import com.vibe.inventory.event.InventoryDeductedEvent;
import com.vibe.order.enums.OrderStatus;
import com.vibe.order.service.OrderStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 库存扣减成功事件监听器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class InventoryDeductedListener extends AbstractEventListener<InventoryDeductedEvent> {
    
    @Autowired
    private OrderStateMachine orderStateMachine;
    
    @Override
    public String getEventType() {
        return "INVENTORY_DEDUCTED";
    }
    
    @Override
    protected void doHandle(InventoryDeductedEvent event) {
        log.info("收到库存扣减成功事件，OrderId: {}, OrderNo: {}", event.getOrderId(), event.getOrderNo());
        
        // 这里可以更新订单状态或记录日志
        // 由于订单创建时已经是待支付状态，这里不需要改变状态
        // 但可以记录库存扣减成功的日志
        
        log.info("库存扣减成功处理完成，OrderId: {}, OrderNo: {}", event.getOrderId(), event.getOrderNo());
    }
}
