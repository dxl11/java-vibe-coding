package com.vibe.payment.dto;

import lombok.Data;

import java.util.Map;

/**
 * 支付回调DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class PaymentCallbackDTO {
    
    /**
     * 支付流水号
     */
    private String paymentNo;
    
    /**
     * 第三方支付流水号
     */
    private String thirdPartyPaymentNo;
    
    /**
     * 支付状态
     */
    private Integer status;
    
    /**
     * 支付金额
     */
    private String amount;
    
    /**
     * 回调数据（原始数据）
     */
    private Map<String, String> callbackParams;
    
    /**
     * 签名
     */
    private String sign;
}
