package com.vibe.payment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 支付创建DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class PaymentCreateDTO {
    
    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    
    /**
     * 订单号
     */
    @NotNull(message = "订单号不能为空")
    private String orderNo;
    
    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private BigDecimal amount;
    
    /**
     * 支付方式：1-支付宝，2-微信，3-银行卡
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;
    
    /**
     * 支付标题
     */
    private String subject;
    
    /**
     * 支付描述
     */
    private String description;
}
