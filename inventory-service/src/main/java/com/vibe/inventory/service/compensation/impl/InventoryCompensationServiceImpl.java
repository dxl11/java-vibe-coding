package com.vibe.inventory.service.compensation.impl;

import com.vibe.inventory.service.InventoryService;
import com.vibe.inventory.service.compensation.InventoryCompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 库存补偿服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class InventoryCompensationServiceImpl implements InventoryCompensationService {
    
    @Autowired
    private InventoryService inventoryService;
    
    @Override
    public void rollbackInventory(Long orderId, String orderNo, Long productId, Integer quantity) {
        log.info("回滚库存，OrderId: {}, OrderNo: {}, ProductId: {}, Quantity: {}", 
                orderId, orderNo, productId, quantity);
        
        try {
            boolean success = inventoryService.rollbackStock(productId, quantity);
            if (success) {
                log.info("库存回滚成功，OrderId: {}, ProductId: {}, Quantity: {}", 
                        orderId, productId, quantity);
            } else {
                log.warn("库存回滚失败，OrderId: {}, ProductId: {}, Quantity: {}", 
                        orderId, productId, quantity);
            }
        } catch (Exception e) {
            log.error("库存回滚异常，OrderId: {}, ProductId: {}, Quantity: {}", 
                    orderId, productId, quantity, e);
        }
    }
}
