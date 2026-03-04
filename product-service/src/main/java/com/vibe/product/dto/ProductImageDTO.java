package com.vibe.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品图片DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class ProductImageDTO {
    
    /**
     * 图片ID
     */
    private Long id;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 图片类型：1-商品图片，2-详情图片
     */
    private Integer imageType;
    
    /**
     * 是否主图：0-否，1-是
     */
    private Integer isMain;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
