package com.vibe.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.vibe.common.core.result.Result;
import com.vibe.order.dto.OrderCreateDTO;
import com.vibe.order.dto.OrderDTO;
import com.vibe.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     * 
     * @param createDTO 创建DTO
     * @return 响应结果
     */
    @PostMapping("/create")
    @SentinelResource(value = "orderCreate")
    public Result<OrderDTO> createOrder(@Validated @RequestBody OrderCreateDTO createDTO) {
        OrderDTO orderDTO = orderService.createOrder(createDTO);
        return Result.success("订单创建成功", orderDTO);
    }
    
    /**
     * 根据订单号查询订单
     * 
     * @param orderNo 订单号
     * @return 响应结果
     */
    @GetMapping("/{orderNo}")
    @SentinelResource(value = "orderGet")
    public Result<OrderDTO> getOrderByOrderNo(@PathVariable String orderNo) {
        OrderDTO orderDTO = orderService.getOrderByOrderNo(orderNo);
        return Result.success(orderDTO);
    }
    
    /**
     * 取消订单
     * 
     * @param orderNo 订单号
     * @return 响应结果
     */
    @PostMapping("/cancel/{orderNo}")
    @SentinelResource(value = "orderCancel")
    public Result<Boolean> cancelOrder(@PathVariable String orderNo) {
        boolean result = orderService.cancelOrder(orderNo);
        return Result.success("订单取消成功", result);
    }
}
