package com.vibe.inventory.api.impl;

import com.vibe.inventory.api.InventoryApi;
import com.vibe.inventory.dto.InventoryDeductDTO;
import com.vibe.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 库存服务API实现类（Dubbo）
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@DubboService
public class InventoryApiImpl implements InventoryApi {
    
    @Autowired
    private InventoryService inventoryService;
    
    @Override
    public boolean deductStock(InventoryDeductDTO deductDTO) {
        return inventoryService.deductStock(deductDTO);
    }
    
    @Override
    public boolean rollbackStock(Long productId, Integer quantity) {
        return inventoryService.rollbackStock(productId, quantity);
    }
    
    @Override
    public Integer getStock(Long productId) {
        return inventoryService.getStock(productId);
    }
}
