package com.vibe.product.service;

import com.vibe.product.dto.ProductImageDTO;

import java.util.List;

/**
 * 商品图片服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface ProductImageService {
    
    /**
     * 添加商品图片
     * 
     * @param imageDTO 图片DTO
     * @return 图片DTO
     */
    ProductImageDTO addImage(ProductImageDTO imageDTO);
    
    /**
     * 删除图片
     * 
     * @param id 图片ID
     * @return 是否成功
     */
    boolean deleteImage(Long id);
    
    /**
     * 设置主图
     * 
     * @param id 图片ID
     * @return 是否成功
     */
    boolean setMainImage(Long id);
    
    /**
     * 根据商品ID查询图片列表
     * 
     * @param productId 商品ID
     * @param imageType 图片类型（可选）
     * @return 图片列表
     */
    List<ProductImageDTO> getImagesByProductId(Long productId, Integer imageType);
    
    /**
     * 批量添加图片
     * 
     * @param productId 商品ID
     * @param imageUrls 图片URL列表
     * @param imageType 图片类型
     * @return 成功数量
     */
    int batchAddImages(Long productId, List<String> imageUrls, Integer imageType);
}
