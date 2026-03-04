package com.vibe.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {
    
    /**
     * 待支付
     */
    PENDING(0, "待支付"),
    
    /**
     * 支付成功
     */
    SUCCESS(1, "支付成功"),
    
    /**
     * 支付失败
     */
    FAILED(2, "支付失败"),
    
    /**
     * 已退款
     */
    REFUNDED(3, "已退款");
    
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
     * @return 支付状态枚举
     */
    public static PaymentStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
