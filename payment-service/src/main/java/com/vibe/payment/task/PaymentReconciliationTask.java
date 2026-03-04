package com.vibe.payment.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.log.LogUtils;
import com.vibe.payment.entity.PaymentRecord;
import com.vibe.payment.enums.PaymentStatus;
import com.vibe.payment.mapper.PaymentRecordMapper;
import com.vibe.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付对账任务
 * 每日对账，检查支付状态差异
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class PaymentReconciliationTask {
    
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * 每日对账任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyReconciliation() {
        log.info("开始执行每日支付对账任务");
        LogUtils.businessLog("PAYMENT_RECONCILIATION", "每日对账开始", LocalDateTime.now());
        
        // 查询前一天待支付的订单
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime today = LocalDateTime.now();
        
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getStatus, PaymentStatus.PENDING.getCode())
                .eq(PaymentRecord::getIsDeleted, 0)
                .between(PaymentRecord::getCreateTime, yesterday, today);
        
        List<PaymentRecord> pendingPayments = paymentRecordMapper.selectList(queryWrapper);
        
        int successCount = 0;
        int failCount = 0;
        
        for (PaymentRecord payment : pendingPayments) {
            try {
                // 主动查询支付状态
                paymentService.queryPaymentStatus(payment.getPaymentNo());
                successCount++;
            } catch (Exception e) {
                log.error("对账失败，支付流水号: {}", payment.getPaymentNo(), e);
                failCount++;
            }
        }
        
        log.info("每日支付对账完成，待支付订单数: {}, 成功: {}, 失败: {}", 
                pendingPayments.size(), successCount, failCount);
        LogUtils.businessLog("PAYMENT_RECONCILIATION", "每日对账完成", 
                pendingPayments.size(), successCount, failCount);
    }
    
    /**
     * 定时查询支付状态
     * 每5分钟执行一次，查询待支付的订单
     */
    @Scheduled(fixedRate = 300000)
    public void queryPendingPayments() {
        log.debug("开始查询待支付订单状态");
        
        // 查询30分钟内创建的待支付订单
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getStatus, PaymentStatus.PENDING.getCode())
                .eq(PaymentRecord::getIsDeleted, 0)
                .ge(PaymentRecord::getCreateTime, thirtyMinutesAgo);
        
        List<PaymentRecord> pendingPayments = paymentRecordMapper.selectList(queryWrapper);
        
        for (PaymentRecord payment : pendingPayments) {
            try {
                paymentService.queryPaymentStatus(payment.getPaymentNo());
            } catch (Exception e) {
                log.error("查询支付状态失败，支付流水号: {}", payment.getPaymentNo(), e);
            }
        }
    }
}
