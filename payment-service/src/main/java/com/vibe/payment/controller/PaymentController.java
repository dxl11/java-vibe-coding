package com.vibe.payment.controller;

import com.vibe.common.core.result.Result;
import com.vibe.payment.dto.PaymentCallbackDTO;
import com.vibe.payment.dto.PaymentCreateDTO;
import com.vibe.payment.dto.PaymentDTO;
import com.vibe.payment.dto.RefundDTO;
import com.vibe.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * 创建支付
     * 
     * @param createDTO 支付创建DTO
     * @return 响应结果
     */
    @PostMapping("/create")
    public Result<PaymentDTO> createPayment(@Validated @RequestBody PaymentCreateDTO createDTO) {
        PaymentDTO payment = paymentService.createPayment(createDTO);
        return Result.success("支付创建成功", payment);
    }
    
    /**
     * 查询支付状态
     * 
     * @param paymentNo 支付流水号
     * @return 响应结果
     */
    @GetMapping("/status/{paymentNo}")
    public Result<PaymentDTO> queryPaymentStatus(@PathVariable String paymentNo) {
        PaymentDTO payment = paymentService.queryPaymentStatus(paymentNo);
        return Result.success(payment);
    }
    
    /**
     * 支付回调（支付宝）
     * 
     * @param params 回调参数
     * @return 响应结果
     */
    @PostMapping("/callback/alipay")
    public String alipayCallback(@RequestParam Map<String, String> params) {
        log.info("收到支付宝支付回调，参数: {}", params);
        
        PaymentCallbackDTO callbackDTO = new PaymentCallbackDTO();
        callbackDTO.setPaymentNo(params.get("out_trade_no"));
        callbackDTO.setThirdPartyPaymentNo(params.get("trade_no"));
        callbackDTO.setAmount(params.get("total_amount"));
        callbackDTO.setCallbackParams(params);
        
        boolean success = paymentService.handlePaymentCallback(callbackDTO);
        
        // 支付宝回调需要返回success或fail
        return success ? "success" : "fail";
    }
    
    /**
     * 支付回调（微信）
     * 
     * @param params 回调参数
     * @return 响应结果
     */
    @PostMapping("/callback/wechat")
    public Map<String, String> wechatCallback(@RequestParam Map<String, String> params) {
        log.info("收到微信支付回调，参数: {}", params);
        
        PaymentCallbackDTO callbackDTO = new PaymentCallbackDTO();
        callbackDTO.setPaymentNo(params.get("out_trade_no"));
        callbackDTO.setThirdPartyPaymentNo(params.get("transaction_id"));
        callbackDTO.setAmount(params.get("total_fee"));
        callbackDTO.setCallbackParams(params);
        
        boolean success = paymentService.handlePaymentCallback(callbackDTO);
        
        // 微信回调需要返回XML格式
        java.util.Map<String, String> result = new java.util.HashMap<>();
        if (success) {
            result.put("return_code", "SUCCESS");
            result.put("return_msg", "OK");
        } else {
            result.put("return_code", "FAIL");
            result.put("return_msg", "处理失败");
        }
        return result;
    }
    
    /**
     * 退款
     * 
     * @param refundDTO 退款DTO
     * @return 响应结果
     */
    @PostMapping("/refund")
    public Result<String> refund(@Validated @RequestBody RefundDTO refundDTO) {
        String refundNo = paymentService.refund(refundDTO);
        return Result.success("退款成功", refundNo);
    }
    
    /**
     * 根据订单号查询支付记录
     * 
     * @param orderNo 订单号
     * @return 响应结果
     */
    @GetMapping("/order/{orderNo}")
    public Result<PaymentDTO> getPaymentByOrderNo(@PathVariable String orderNo) {
        PaymentDTO payment = paymentService.getPaymentByOrderNo(orderNo);
        return Result.success(payment);
    }
}
