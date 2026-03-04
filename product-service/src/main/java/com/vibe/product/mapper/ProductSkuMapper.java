package com.vibe.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.product.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品SKU Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
    
    /**
     * 扣减SKU库存
     * 
     * @param skuId SKU ID
     * @param quantity 数量
     * @return 更新行数
     */
    @Update("UPDATE product_sku SET stock = stock - #{quantity}, update_time = NOW() " +
            "WHERE id = #{skuId} AND stock >= #{quantity} AND is_deleted = 0")
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
    
    /**
     * 回滚SKU库存
     * 
     * @param skuId SKU ID
     * @param quantity 数量
     * @return 更新行数
     */
    @Update("UPDATE product_sku SET stock = stock + #{quantity}, update_time = NOW() " +
            "WHERE id = #{skuId} AND is_deleted = 0")
    int rollbackStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
