package com.vibe.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商品SKU DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class ProductSkuDTO {
    
    /**
     * SKU ID
     */
    private Long id;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * SKU编码
     */
    private String skuCode;
    
    /**
     * SKU名称
     */
    private String skuName;
    
    /**
     * 规格组合（Map格式）
     */
    private Map<String, String> specs;
    
    /**
     * SKU价格
     */
    private BigDecimal price;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * SKU图片
     */
    private String image;
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
