package com.vibe.order.controller;

import com.vibe.common.core.result.Result;
import com.vibe.common.core.saga.recovery.SagaRecoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SAGA 恢复控制器
 * 提供手动恢复接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/saga/recovery")
public class SagaRecoveryController {
    
    @Autowired
    private SagaRecoveryService sagaRecoveryService;
    
    /**
     * 手动恢复事务
     * 
     * @param transactionId 事务ID
     * @return 恢复结果
     */
    @PostMapping("/transaction")
    public Result<Boolean> recoverTransaction(@RequestParam String transactionId) {
        boolean success = sagaRecoveryService.recoverTransaction(transactionId);
        if (success) {
            return Result.success("事务恢复成功", true);
        } else {
            return Result.fail(400, "事务恢复失败");
        }
    }
    
    /**
     * 根据业务ID恢复事务
     * 
     * @param businessId 业务ID（如订单号）
     * @return 恢复结果
     */
    @PostMapping("/business")
    public Result<Boolean> recoverByBusinessId(@RequestParam String businessId) {
        boolean success = sagaRecoveryService.recoverByBusinessId(businessId);
        if (success) {
            return Result.success("事务恢复成功", true);
        } else {
            return Result.fail(400, "事务恢复失败");
        }
    }
}
