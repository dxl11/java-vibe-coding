package com.vibe.product.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class ProductCategoryDTO {
    
    /**
     * 分类ID
     */
    private Long id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 父分类ID
     */
    private Long parentId;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 子分类列表
     */
    private List<ProductCategoryDTO> children;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
