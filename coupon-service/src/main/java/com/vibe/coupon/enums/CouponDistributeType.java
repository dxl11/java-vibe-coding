package com.vibe.coupon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券发放方式枚举
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Getter
@AllArgsConstructor
public enum CouponDistributeType {
    
    /**
     * 手动发放
     */
    MANUAL(1, "手动发放"),
    
    /**
     * 自动发放
     */
    AUTO(2, "自动发放"),
    
    /**
     * 活动发放
     */
    ACTIVITY(3, "活动发放");
    
    /**
     * 发放方式码
     */
    private final Integer code;
    
    /**
     * 发放方式描述
     */
    private final String description;
    
    /**
     * 根据发放方式码获取枚举
     * 
     * @param code 发放方式码
     * @return 优惠券发放方式枚举
     */
    public static CouponDistributeType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CouponDistributeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
