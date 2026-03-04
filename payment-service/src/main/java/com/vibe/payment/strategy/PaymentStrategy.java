package com.vibe.payment.strategy;

import com.vibe.payment.dto.PaymentCreateDTO;
import com.vibe.payment.dto.PaymentDTO;

/**
 * 支付策略接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface PaymentStrategy {
    
    /**
     * 创建支付
     * 
     * @param createDTO 支付创建DTO
     * @return 支付DTO（包含支付URL）
     */
    PaymentDTO createPayment(PaymentCreateDTO createDTO);
    
    /**
     * 查询支付状态
     * 
     * @param paymentNo 支付流水号
     * @return 支付状态
     */
    Integer queryPaymentStatus(String paymentNo);
    
    /**
     * 处理支付回调
     * 
     * @param callbackParams 回调参数
     * @return 是否处理成功
     */
    boolean handleCallback(java.util.Map<String, String> callbackParams);
    
    /**
     * 退款
     * 
     * @param paymentNo 支付流水号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款流水号
     */
    String refund(String paymentNo, java.math.BigDecimal refundAmount, String refundReason);
}
