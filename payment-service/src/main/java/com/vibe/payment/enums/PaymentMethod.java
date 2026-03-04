package com.vibe.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Getter
@AllArgsConstructor
public enum PaymentMethod {
    
    /**
     * 支付宝
     */
    ALIPAY(1, "支付宝"),
    
    /**
     * 微信支付
     */
    WECHAT(2, "微信支付"),
    
    /**
     * 银行卡
     */
    BANK_CARD(3, "银行卡");
    
    /**
     * 支付方式码
     */
    private final Integer code;
    
    /**
     * 支付方式描述
     */
    private final String description;
    
    /**
     * 根据支付方式码获取枚举
     * 
     * @param code 支付方式码
     * @return 支付方式枚举
     */
    public static PaymentMethod getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentMethod method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }
}
