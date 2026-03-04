package com.vibe.coupon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户优惠券状态枚举
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Getter
@AllArgsConstructor
public enum UserCouponStatus {
    
    /**
     * 未使用
     */
    UNUSED(0, "未使用"),
    
    /**
     * 已使用
     */
    USED(1, "已使用"),
    
    /**
     * 已过期
     */
    EXPIRED(2, "已过期");
    
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
     * @return 用户优惠券状态枚举
     */
    public static UserCouponStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserCouponStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
