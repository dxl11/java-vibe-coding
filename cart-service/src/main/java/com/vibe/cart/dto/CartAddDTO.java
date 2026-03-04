package com.vibe.cart.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 购物车添加DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class CartAddDTO {
    
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    /**
     * SKU ID（可选）
     */
    private Long skuId;
    
    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空")
    @Positive(message = "购买数量必须大于0")
    private Integer quantity;
}
