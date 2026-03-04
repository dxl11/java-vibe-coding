package com.vibe.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class ProductDTO {
    
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
    private BigDecimal price;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 状态：0-下架，1-上架，2-待审核，3-已驳回
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
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
    
    /**
     * 销量
     */
    private Integer sales;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
