package com.vibe.inventory.service;

import com.vibe.inventory.dto.InventoryDeductDTO;

/**
 * 库存服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface InventoryService {
    
    /**
     * 扣减库存
     * 
     * @param deductDTO 扣减DTO
     * @return 是否成功
     */
    boolean deductStock(InventoryDeductDTO deductDTO);
    
    /**
     * 回滚库存
     * 
     * @param productId 商品ID
     * @param quantity 回滚数量
     * @return 是否成功
     */
    boolean rollbackStock(Long productId, Integer quantity);
    
    /**
     * 查询库存
     * 
     * @param productId 商品ID
     * @return 库存数量
     */
    Integer getStock(Long productId);
}
