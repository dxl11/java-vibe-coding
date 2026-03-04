package com.vibe.payment.service;

import com.vibe.payment.dto.PaymentCallbackDTO;
import com.vibe.payment.dto.PaymentCreateDTO;
import com.vibe.payment.dto.PaymentDTO;
import com.vibe.payment.dto.RefundDTO;

/**
 * 支付服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface PaymentService {
    
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
     * @return 支付DTO
     */
    PaymentDTO queryPaymentStatus(String paymentNo);
    
    /**
     * 处理支付回调
     * 
     * @param callbackDTO 回调DTO
     * @return 是否处理成功
     */
    boolean handlePaymentCallback(PaymentCallbackDTO callbackDTO);
    
    /**
     * 退款
     * 
     * @param refundDTO 退款DTO
     * @return 退款流水号
     */
    String refund(RefundDTO refundDTO);
    
    /**
     * 根据订单号查询支付记录
     * 
     * @param orderNo 订单号
     * @return 支付DTO
     */
    PaymentDTO getPaymentByOrderNo(String orderNo);
}
