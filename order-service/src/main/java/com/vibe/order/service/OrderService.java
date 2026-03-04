package com.vibe.order.service;

import com.vibe.order.dto.OrderCreateDTO;
import com.vibe.order.dto.OrderDTO;

/**
 * 订单服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface OrderService {
    
    /**
     * 创建订单
     * 
     * @param createDTO 创建DTO
     * @return 订单DTO
     */
    OrderDTO createOrder(OrderCreateDTO createDTO);
    
    /**
     * 根据订单号查询订单
     * 
     * @param orderNo 订单号
     * @return 订单DTO
     */
    OrderDTO getOrderByOrderNo(String orderNo);
    
    /**
     * 取消订单
     * 
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean cancelOrder(String orderNo);
}
