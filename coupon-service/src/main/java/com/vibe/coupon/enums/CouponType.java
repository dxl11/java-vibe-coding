package com.vibe.coupon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券类型枚举
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Getter
@AllArgsConstructor
public enum CouponType {
    
    /**
     * 满减券
     */
    FULL_REDUCTION(1, "满减券"),
    
    /**
     * 折扣券
     */
    DISCOUNT(2, "折扣券"),
    
    /**
     * 免邮券
     */
    FREE_SHIPPING(3, "免邮券");
    
    /**
     * 类型码
     */
    private final Integer code;
    
    /**
     * 类型描述
     */
    private final String description;
    
    /**
     * 根据类型码获取枚举
     * 
     * @param code 类型码
     * @return 优惠券类型枚举
     */
    public static CouponType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CouponType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
