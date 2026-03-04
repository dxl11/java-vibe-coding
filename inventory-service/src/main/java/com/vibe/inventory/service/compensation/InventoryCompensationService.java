package com.vibe.inventory.service.compensation;

/**
 * 库存补偿服务接口
 * 处理库存相关的补偿操作
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface InventoryCompensationService {
    
    /**
     * 回滚库存扣减
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param productId 商品ID
     * @param quantity 回滚数量
     */
    void rollbackInventory(Long orderId, String orderNo, Long productId, Integer quantity);
}
