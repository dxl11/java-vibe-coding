package com.vibe.payment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 退款DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class RefundDTO {
    
    /**
     * 支付记录ID
     */
    @NotNull(message = "支付记录ID不能为空")
    private Long paymentId;
    
    /**
     * 支付流水号
     */
    @NotNull(message = "支付流水号不能为空")
    private String paymentNo;
    
    /**
     * 退款金额（为空则全额退款）
     */
    @Positive(message = "退款金额必须大于0")
    private BigDecimal refundAmount;
    
    /**
     * 退款原因
     */
    private String refundReason;
}
