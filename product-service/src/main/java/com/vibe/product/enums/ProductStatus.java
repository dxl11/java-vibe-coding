package com.vibe.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Getter
@AllArgsConstructor
public enum ProductStatus {
    
    /**
     * 下架
     */
    OFFLINE(0, "下架"),
    
    /**
     * 上架
     */
    ONLINE(1, "上架"),
    
    /**
     * 待审核
     */
    PENDING_AUDIT(2, "待审核"),
    
    /**
     * 已驳回
     */
    REJECTED(3, "已驳回");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 根据状态码获取枚举
     * 
     * @param code 状态码
     * @return 商品状态枚举
     */
    public static ProductStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
