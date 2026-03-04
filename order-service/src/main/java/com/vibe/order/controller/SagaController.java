package com.vibe.order.controller;

import com.vibe.common.core.result.Result;
import com.vibe.common.core.saga.SagaTransactionManager;
import com.vibe.common.core.saga.entity.SagaTransaction;
import com.vibe.common.core.saga.entity.SagaStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SAGA 事务查询控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/saga")
public class SagaController {
    
    @Autowired
    private SagaTransactionManager sagaTransactionManager;
    
    /**
     * 根据事务ID查询事务详情
     * 
     * @param transactionId 事务ID
     * @return 事务详情
     */
    @GetMapping("/transaction/{transactionId}")
    public Result<Map<String, Object>> getTransaction(@PathVariable String transactionId) {
        SagaTransaction transaction = sagaTransactionManager.getTransaction(transactionId);
        if (transaction == null) {
            return Result.fail(404, "事务不存在");
        }
        
        List<SagaStep> steps = sagaTransactionManager.getSteps(transactionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("transaction", transaction);
        result.put("steps", steps);
        
        return Result.success("查询成功", result);
    }
    
    /**
     * 根据业务ID查询事务列表
     * 
     * @param businessId 业务ID（如订单号）
     * @return 事务列表
     */
    @GetMapping("/transaction/business/{businessId}")
    public Result<List<SagaTransaction>> getTransactionsByBusinessId(@PathVariable String businessId) {
        List<SagaTransaction> transactions = sagaTransactionManager.getTransactionsByBusinessId(businessId);
        return Result.success("查询成功", transactions);
    }
}
