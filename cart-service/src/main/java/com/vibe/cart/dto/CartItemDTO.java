package com.vibe.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class CartItemDTO {
    
    /**
     * 购物车项ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * SKU ID
     */
    private Long skuId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品图片
     */
    private String productImage;
    
    /**
     * 商品价格
     */
    private BigDecimal productPrice;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 小计金额
     */
    private BigDecimal subtotal;
    
    /**
     * 是否选中：0-未选中，1-选中
     */
    private Integer selected;
    
    /**
     * 库存数量（实时查询）
     */
    private Integer stock;
    
    /**
     * 是否有效（库存、价格校验）
     */
    private Boolean valid;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
