package com.vibe.product.dto;

import lombok.Data;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 商品更新DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class ProductUpdateDTO {
    
    /**
     * 商品ID
     */
    private Long id;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品价格
     */
    @Positive(message = "商品价格必须大于0")
    private BigDecimal price;
    
    /**
     * 库存数量
     */
    @Positive(message = "库存数量必须大于0")
    private Integer stock;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 商品详情（富文本）
     */
    private String detail;
    
    /**
     * SEO关键词
     */
    private String seoKeywords;
    
    /**
     * SEO描述
     */
    private String seoDescription;
}
