package com.vibe.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class PaymentDTO {
    
    /**
     * 支付记录ID
     */
    private Long id;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 支付流水号
     */
    private String paymentNo;
    
    /**
     * 支付金额
     */
    private BigDecimal amount;
    
    /**
     * 支付方式：1-支付宝，2-微信，3-银行卡
     */
    private Integer paymentMethod;
    
    /**
     * 支付方式描述
     */
    private String paymentMethodDesc;
    
    /**
     * 支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款
     */
    private Integer status;
    
    /**
     * 支付状态描述
     */
    private String statusDesc;
    
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    
    /**
     * 第三方支付流水号
     */
    private String thirdPartyPaymentNo;
    
    /**
     * 支付URL（用于跳转支付）
     */
    private String payUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
