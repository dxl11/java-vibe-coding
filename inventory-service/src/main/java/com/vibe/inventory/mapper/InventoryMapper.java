package com.vibe.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 库存Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
    
    /**
     * 扣减库存
     * 
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 影响行数
     */
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 回滚库存
     * 
     * @param productId 商品ID
     * @param quantity 回滚数量
     * @return 影响行数
     */
    int rollbackStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
