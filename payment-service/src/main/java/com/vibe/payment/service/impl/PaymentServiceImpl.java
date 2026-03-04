package com.vibe.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.idempotent.IdempotentUtils;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.common.core.util.MoneyUtils;
import com.vibe.payment.dto.PaymentCallbackDTO;
import com.vibe.payment.dto.PaymentCreateDTO;
import com.vibe.payment.dto.PaymentDTO;
import com.vibe.payment.dto.RefundDTO;
import com.vibe.payment.entity.PaymentRecord;
import com.vibe.payment.enums.PaymentMethod;
import com.vibe.payment.enums.PaymentStatus;
import com.vibe.payment.mapper.PaymentRecordMapper;
import com.vibe.payment.service.PaymentService;
import com.vibe.payment.strategy.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    
    @Autowired
    @Qualifier("alipayPaymentStrategy")
    private PaymentStrategy alipayPaymentStrategy;
    
    @Autowired
    @Qualifier("wechatPaymentStrategy")
    private PaymentStrategy wechatPaymentStrategy;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private RefundRecordMapper refundRecordMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "PAYMENT_CREATE", resource = "PAYMENT", action = "CREATE")
    @MonitorPerformance(threshold = 2000, operation = "创建支付")
    public PaymentDTO createPayment(PaymentCreateDTO createDTO) {
        LogUtils.businessLog("PAYMENT_CREATE", "创建支付开始", createDTO.getOrderNo());
        
        // 幂等性检查
        String idempotentKey = "payment:idempotent:create:" + createDTO.getOrderNo();
        boolean firstRequest = IdempotentUtils.checkAndSet(redisTemplate, idempotentKey, "1", 300);
        if (!firstRequest) {
            log.warn("检测到重复的创建支付请求，订单号: {}", createDTO.getOrderNo());
            throw new BusinessException(429, "请勿重复提交支付");
        }
        
        // 检查是否已存在支付记录
        PaymentRecord existRecord = paymentRecordMapper.selectByOrderNo(createDTO.getOrderNo());
        if (existRecord != null && existRecord.getStatus() == PaymentStatus.SUCCESS.getCode()) {
            throw new BusinessException(400, "订单已支付");
        }
        
        // 生成支付流水号
        String paymentNo = "PAY" + IdUtil.getSnowflake(1, 1).nextIdStr();
        
        // 选择支付策略
        PaymentStrategy paymentStrategy = getPaymentStrategy(createDTO.getPaymentMethod());
        
        // 创建支付记录
        PaymentRecord paymentRecord = PaymentRecord.builder()
                .orderId(createDTO.getOrderId())
                .orderNo(createDTO.getOrderNo())
                .paymentNo(paymentNo)
                .amount(MoneyUtils.createMoney(createDTO.getAmount()))
                .paymentMethod(createDTO.getPaymentMethod())
                .status(PaymentStatus.PENDING.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        paymentRecordMapper.insert(paymentRecord);
        
        // 调用支付策略创建支付
        PaymentDTO paymentDTO = paymentStrategy.createPayment(createDTO);
        paymentDTO.setPaymentNo(paymentNo);
        paymentDTO.setOrderId(createDTO.getOrderId());
        paymentDTO.setOrderNo(createDTO.getOrderNo());
        paymentDTO.setAmount(createDTO.getAmount());
        paymentDTO.setPaymentMethod(createDTO.getPaymentMethod());
        paymentDTO.setStatus(PaymentStatus.PENDING.getCode());
        
        PaymentMethod method = PaymentMethod.getByCode(createDTO.getPaymentMethod());
        if (method != null) {
            paymentDTO.setPaymentMethodDesc(method.getDescription());
        }
        
        LogUtils.businessLog("PAYMENT_CREATE", "创建支付成功", paymentNo, createDTO.getOrderNo());
        
        return paymentDTO;
    }
    
    @Override
    @MonitorPerformance(threshold = 1000, operation = "查询支付状态")
    public PaymentDTO queryPaymentStatus(String paymentNo) {
        PaymentRecord paymentRecord = paymentRecordMapper.selectByPaymentNo(paymentNo);
        if (paymentRecord == null) {
            throw new BusinessException(404, "支付记录不存在");
        }
        
        // 如果状态是待支付，主动查询第三方支付状态
        if (paymentRecord.getStatus() == PaymentStatus.PENDING.getCode()) {
            PaymentStrategy paymentStrategy = getPaymentStrategy(paymentRecord.getPaymentMethod());
            Integer status = paymentStrategy.queryPaymentStatus(paymentNo);
            
            if (status != null && status != paymentRecord.getStatus()) {
                // 更新支付状态
                paymentRecordMapper.updatePaymentStatus(
                        paymentNo, 
                        status, 
                        paymentRecord.getThirdPartyPaymentNo());
            }
        }
        
        return convertToDTO(paymentRecord);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "PAYMENT_CALLBACK", resource = "PAYMENT", action = "UPDATE")
    @MonitorPerformance(threshold = 1000, operation = "支付回调处理")
    public boolean handlePaymentCallback(PaymentCallbackDTO callbackDTO) {
        LogUtils.businessLog("PAYMENT_CALLBACK", "处理支付回调开始", callbackDTO.getPaymentNo());
        
        // 幂等性检查
        String idempotentKey = "payment:callback:idempotent:" + callbackDTO.getPaymentNo();
        boolean firstRequest = IdempotentUtils.checkAndSet(redisTemplate, idempotentKey, "1", 86400);
        if (!firstRequest) {
            log.warn("检测到重复的支付回调，支付流水号: {}", callbackDTO.getPaymentNo());
            return true;  // 已处理过，返回成功
        }
        
        PaymentRecord paymentRecord = paymentRecordMapper.selectByPaymentNo(callbackDTO.getPaymentNo());
        if (paymentRecord == null) {
            log.error("支付记录不存在，支付流水号: {}", callbackDTO.getPaymentNo());
            return false;
        }
        
        // 如果已经支付成功，直接返回
        if (paymentRecord.getStatus() == PaymentStatus.SUCCESS.getCode()) {
            log.info("支付已成功，无需重复处理，支付流水号: {}", callbackDTO.getPaymentNo());
            return true;
        }
        
        // 选择支付策略处理回调
        PaymentStrategy paymentStrategy = getPaymentStrategy(paymentRecord.getPaymentMethod());
        boolean success = paymentStrategy.handleCallback(callbackDTO.getCallbackParams());
        
        if (success) {
            // 更新支付状态
            paymentRecordMapper.updatePaymentStatus(
                    callbackDTO.getPaymentNo(),
                    PaymentStatus.SUCCESS.getCode(),
                    callbackDTO.getThirdPartyPaymentNo());
            
            // TODO: 发布支付成功事件，通知订单服务
            // eventPublisher.publishPaymentSuccessEvent(...);
            
            LogUtils.businessLog("PAYMENT_CALLBACK", "支付回调处理成功", callbackDTO.getPaymentNo());
        } else {
            // 更新为支付失败
            paymentRecordMapper.updatePaymentStatus(
                    callbackDTO.getPaymentNo(),
                    PaymentStatus.FAILED.getCode(),
                    callbackDTO.getThirdPartyPaymentNo());
            
            LogUtils.businessLog("PAYMENT_CALLBACK", "支付回调处理失败", callbackDTO.getPaymentNo());
        }
        
        return success;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "PAYMENT_REFUND", resource = "PAYMENT", action = "UPDATE")
    @MonitorPerformance(threshold = 2000, operation = "支付退款")
    public String refund(RefundDTO refundDTO) {
        LogUtils.businessLog("PAYMENT_REFUND", "退款开始", refundDTO.getPaymentNo());
        
        PaymentRecord paymentRecord = paymentRecordMapper.selectByPaymentNo(refundDTO.getPaymentNo());
        if (paymentRecord == null) {
            throw new BusinessException(404, "支付记录不存在");
        }
        
        if (paymentRecord.getStatus() != PaymentStatus.SUCCESS.getCode()) {
            throw new BusinessException(400, "只有支付成功的订单才能退款");
        }
        
        // 计算退款金额（为空则全额退款）
        java.math.BigDecimal refundAmount = refundDTO.getRefundAmount();
        if (refundAmount == null) {
            refundAmount = paymentRecord.getAmount();
        }
        
        if (refundAmount.compareTo(paymentRecord.getAmount()) > 0) {
            throw new BusinessException(400, "退款金额不能大于支付金额");
        }
        
        // 选择支付策略进行退款
        PaymentStrategy paymentStrategy = getPaymentStrategy(paymentRecord.getPaymentMethod());
        String refundNo = paymentStrategy.refund(refundDTO.getPaymentNo(), refundAmount, refundDTO.getRefundReason());
        
        // 创建退款记录
        RefundRecord refundRecord = RefundRecord.builder()
                .paymentId(paymentRecord.getId())
                .paymentNo(refundDTO.getPaymentNo())
                .refundNo(refundNo)
                .refundAmount(MoneyUtils.createMoney(refundAmount))
                .refundReason(refundDTO.getRefundReason())
                .status(0)  // 处理中
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        refundRecordMapper.insert(refundRecord);
        
        // 更新支付状态为已退款（全额退款时）
        if (refundAmount.compareTo(paymentRecord.getAmount()) == 0) {
            paymentRecordMapper.updatePaymentStatus(
                    refundDTO.getPaymentNo(),
                    PaymentStatus.REFUNDED.getCode(),
                    refundNo);
        }
        
        // TODO: 发布退款成功事件
        // eventPublisher.publishRefundSuccessEvent(...);
        
        LogUtils.businessLog("PAYMENT_REFUND", "退款成功", refundNo, refundDTO.getPaymentNo());
        
        return refundNo;
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "查询支付记录")
    public PaymentDTO getPaymentByOrderNo(String orderNo) {
        PaymentRecord paymentRecord = paymentRecordMapper.selectByOrderNo(orderNo);
        if (paymentRecord == null) {
            throw new BusinessException(404, "支付记录不存在");
        }
        return convertToDTO(paymentRecord);
    }
    
    /**
     * 获取支付策略
     * 
     * @param paymentMethod 支付方式
     * @return 支付策略
     */
    private PaymentStrategy getPaymentStrategy(Integer paymentMethod) {
        PaymentMethod method = PaymentMethod.getByCode(paymentMethod);
        if (method == null) {
            throw new BusinessException(400, "不支持的支付方式");
        }
        
        switch (method) {
            case ALIPAY:
                return alipayPaymentStrategy;
            case WECHAT:
                return wechatPaymentStrategy;
            case BANK_CARD:
                throw new BusinessException(400, "银行卡支付暂未支持");
            default:
                throw new BusinessException(400, "不支持的支付方式");
        }
    }
    
    /**
     * 转换为DTO
     * 
     * @param paymentRecord 支付记录
     * @return 支付DTO
     */
    private PaymentDTO convertToDTO(PaymentRecord paymentRecord) {
        PaymentDTO dto = new PaymentDTO();
        BeanUtils.copyProperties(paymentRecord, dto);
        
        PaymentMethod method = PaymentMethod.getByCode(paymentRecord.getPaymentMethod());
        if (method != null) {
            dto.setPaymentMethodDesc(method.getDescription());
        }
        
        PaymentStatus status = PaymentStatus.getByCode(paymentRecord.getStatus());
        if (status != null) {
            dto.setStatusDesc(status.getDescription());
        }
        
        return dto;
    }
}
