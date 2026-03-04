package com.vibe.product.service;

import com.vibe.product.dto.ProductSkuDTO;

import java.util.List;

/**
 * 商品SKU服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface ProductSkuService {
    
    /**
     * 创建SKU
     * 
     * @param skuDTO SKU DTO
     * @return SKU DTO
     */
    ProductSkuDTO createSku(ProductSkuDTO skuDTO);
    
    /**
     * 更新SKU
     * 
     * @param skuDTO SKU DTO
     * @return SKU DTO
     */
    ProductSkuDTO updateSku(ProductSkuDTO skuDTO);
    
    /**
     * 根据ID查询SKU
     * 
     * @param id SKU ID
     * @return SKU DTO
     */
    ProductSkuDTO getSkuById(Long id);
    
    /**
     * 根据商品ID查询SKU列表
     * 
     * @param productId 商品ID
     * @return SKU列表
     */
    List<ProductSkuDTO> getSkusByProductId(Long productId);
    
    /**
     * 删除SKU
     * 
     * @param id SKU ID
     * @return 是否成功
     */
    boolean deleteSku(Long id);
    
    /**
     * 扣减SKU库存
     * 
     * @param skuId SKU ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean deductStock(Long skuId, Integer quantity);
    
    /**
     * 回滚SKU库存
     * 
     * @param skuId SKU ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean rollbackStock(Long skuId, Integer quantity);
}
