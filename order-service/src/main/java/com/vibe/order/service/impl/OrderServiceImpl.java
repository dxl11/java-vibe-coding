package com.vibe.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.vibe.common.core.event.EventPublisher;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.idempotent.IdempotentUtils;
import com.vibe.common.core.idempotent.DistributedLock;
import com.vibe.common.core.cache.CacheUtils;
import com.vibe.common.core.util.MoneyUtils;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.common.core.saga.SagaTransactionManager;
import com.vibe.order.enums.OrderStatus;
import com.vibe.order.event.OrderCreatedEvent;
import com.vibe.order.service.OrderStateMachine;
import com.vibe.order.dto.OrderCreateDTO;
import com.vibe.order.dto.OrderDTO;
import com.vibe.order.entity.Order;
import com.vibe.order.entity.OrderItem;
import com.vibe.order.mapper.OrderItemMapper;
import com.vibe.order.mapper.OrderMapper;
import com.vibe.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private OrderStateMachine orderStateMachine;
    
    @Autowired
    private EventPublisher eventPublisher;
    
    @Autowired
    private SagaTransactionManager sagaTransactionManager;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private com.vibe.order.service.compensation.OrderCompensationService orderCompensationService;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "ORDER_CREATE", resource = "ORDER", action = "CREATE")
    @MonitorPerformance(threshold = 3000, operation = "订单创建")
    public OrderDTO createOrder(OrderCreateDTO createDTO) {
        LogUtils.businessLog("ORDER_CREATE", "创建订单开始", 
                createDTO.getUserId(), createDTO.getItems().size());
        
        // 请求幂等性校验（基于请求ID）
        String requestId = createDTO.getRequestId();
        String idempotentKey = "order:idempotent:create:" + createDTO.getUserId() + ":" + requestId;
        boolean firstRequest = IdempotentUtils.checkAndSet(redisTemplate, idempotentKey, "1", 300);
        if (!firstRequest) {
            log.warn("检测到重复的创建订单请求，userId: {}, requestId: {}", createDTO.getUserId(), requestId);
            throw new BusinessException(429, "请勿重复提交订单");
        }
        
        // 分布式锁（防止同一用户并发创建订单导致的并发问题，带自动续期）
        String lockKey = "order:lock:create:" + createDTO.getUserId();
        DistributedLock distributedLock = IdempotentUtils.tryLockWithRenewal(redisTemplate, lockKey, 30);
        if (distributedLock == null) {
            log.warn("获取创建订单分布式锁失败，userId: {}", createDTO.getUserId());
            throw new BusinessException(503, "系统繁忙，请稍后重试");
        }
        
        // 生成订单号
        String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();
        
        // 计算订单总金额（这里简化处理，实际应该调用商品服务获取商品信息）
        BigDecimal totalAmount = MoneyUtils.createMoney("0.00");
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderCreatedEvent.OrderItemInfo> eventItems = new ArrayList<>();
        
        for (OrderCreateDTO.OrderItemDTO itemDTO : createDTO.getItems()) {
            // 参数校验
            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new BusinessException(400, "商品数量必须大于0");
            }
            if (itemDTO.getQuantity() > 999) {
                throw new BusinessException(400, "单次购买数量不能超过999");
            }
            
            // TODO: 调用商品服务获取商品信息
            // 这里简化处理，假设商品价格为100
            BigDecimal productPrice = MoneyUtils.createMoney("100.00");
            BigDecimal subtotal = MoneyUtils.multiply(productPrice, itemDTO.getQuantity());
            totalAmount = MoneyUtils.add(totalAmount, subtotal);
            
            // 创建订单明细
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemDTO.getProductId())
                    .productName("商品名称")  // TODO: 从商品服务获取
                    .productPrice(productPrice)
                    .quantity(itemDTO.getQuantity())
                    .subtotal(subtotal)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .isDeleted(0)
                    .build();
            orderItems.add(orderItem);
            
            // 构建事件明细
            OrderCreatedEvent.OrderItemInfo itemInfo = OrderCreatedEvent.OrderItemInfo.builder()
                    .productId(itemDTO.getProductId())
                    .productName("商品名称")
                    .quantity(itemDTO.getQuantity())
                    .price(productPrice)
                    .build();
            eventItems.add(itemInfo);
        }
        
        // 计算优惠券金额（这里简化处理）
        BigDecimal couponAmount = MoneyUtils.createMoney("0.00");
        if (createDTO.getCouponId() != null) {
            // TODO: 调用优惠券服务计算优惠金额
            couponAmount = MoneyUtils.createMoney("10.00");
        }
        
        // 计算实付金额（确保不为负数）
        BigDecimal payAmount = MoneyUtils.subtract(totalAmount, couponAmount);
        payAmount = MoneyUtils.ensureNonNegative(payAmount);
        
        try {
            // 创建订单（状态：待支付）
            Order order = Order.builder()
                    .orderNo(orderNo)
                    .userId(createDTO.getUserId())
                    .totalAmount(totalAmount)
                    .payAmount(payAmount)
                    .couponAmount(couponAmount)
                    .status(OrderStatus.PENDING_PAYMENT.getCode())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .isDeleted(0)
                    .build();
            
            orderMapper.insert(order);
            
            // 批量保存订单明细（性能优化）
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrderId(order.getId());
            }
            // 使用MyBatis-Plus的批量插入
            if (!orderItems.isEmpty()) {
                orderItemMapper.insertBatch(orderItems);
            }
            
            // 记录状态流转
            orderStateMachine.transition(order.getId(), orderNo, null, OrderStatus.PENDING_PAYMENT, 
                    "ORDER_CREATE", "{\"userId\":" + createDTO.getUserId() + "}");
            
            LogUtils.businessLog("ORDER_CREATE", "订单创建成功", 
                    orderNo, order.getId(), createDTO.getUserId(), payAmount);
            
            // 创建 SAGA 事务
            String transactionId = sagaTransactionManager.createTransaction(
                    orderNo, "ORDER_CREATE", 300);  // 5分钟超时
            
            // 更新事务状态为处理中
            sagaTransactionManager.updateTransactionStatus(
                    transactionId, 
                    com.vibe.common.core.saga.entity.SagaTransaction.TransactionStatus.PROCESSING);
            
            // 发布订单创建事件（异步处理库存扣减、优惠券锁定等）
            OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                    .orderId(order.getId())
                    .orderNo(orderNo)
                    .userId(createDTO.getUserId())
                    .totalAmount(totalAmount)
                    .payAmount(payAmount)
                    .couponId(createDTO.getCouponId())
                    .items(eventItems)
                    .build();
            
            // 设置父类字段
            orderCreatedEvent.setEventType("ORDER_CREATED");
            orderCreatedEvent.setSource("order-service");
            orderCreatedEvent.setBusinessId(transactionId);
            orderCreatedEvent.setEventTime(java.time.LocalDateTime.now());
            
            eventPublisher.publishOrderEvent("order-create", orderCreatedEvent);
            
            // 转换为DTO
            OrderDTO orderDTO = new OrderDTO();
            BeanUtils.copyProperties(order, orderDTO);
            orderDTO.setItems(orderItems.stream().map(item -> {
                OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
                BeanUtils.copyProperties(item, itemDTO);
                return itemDTO;
            }).collect(Collectors.toList()));
            
            // 清除缓存（订单创建后，缓存可能不一致）
            CacheUtils.delete(redisTemplate, "order:detail:" + orderNo);
            
            return orderDTO;
        } catch (Exception e) {
            // 业务失败时清理幂等key，允许重试
            IdempotentUtils.release(redisTemplate, idempotentKey);
            throw e;
        } finally {
            // 释放分布式锁（会自动停止续期任务）
            if (distributedLock != null) {
                distributedLock.unlock();
            }
        }
    }
    
    @Override
    @org.springframework.cache.annotation.Cacheable(value = "order:detail", key = "#orderNo", 
            unless = "#result == null")
    public OrderDTO getOrderByOrderNo(String orderNo) {
        // 先查缓存（防止缓存穿透）
        String cacheKey = "order:detail:" + orderNo;
        String cachedValue = CacheUtils.get(redisTemplate, cacheKey);
        
        if (cachedValue != null) {
            if (cachedValue.equals("__NULL__")) {
                // 空值缓存，说明之前查询过但不存在
                throw new BusinessException(404, "订单不存在");
            }
            // TODO: 反序列化缓存值返回（这里简化处理，直接查数据库）
        }
        
        Order order = orderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getIsDeleted, 0)
        );
        
        if (order == null) {
            // 设置空值缓存，防止缓存穿透
            CacheUtils.setNullValue(redisTemplate, cacheKey);
            throw new BusinessException(404, "订单不存在");
        }
        
        // 查询订单明细
        java.util.List<OrderItem> orderItems = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>() 
                        .eq(OrderItem::getOrderId, order.getId())
                        .eq(OrderItem::getIsDeleted, 0)
        );
        
        OrderDTO orderDTO = new OrderDTO();
        BeanUtils.copyProperties(order, orderDTO);
        orderDTO.setItems(orderItems.stream().map(item -> {
            OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
            BeanUtils.copyProperties(item, itemDTO);
            return itemDTO;
        }).collect(Collectors.toList()));
        
        return orderDTO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "ORDER_CANCEL", resource = "ORDER", action = "CANCEL")
    @MonitorPerformance(threshold = 2000, operation = "订单取消")
    public boolean cancelOrder(String orderNo) {
        LogUtils.businessLog("ORDER_CANCEL", "取消订单开始", orderNo);
        
        // 分布式锁（防止并发取消同一订单，带自动续期）
        String lockKey = "order:lock:cancel:" + orderNo;
        DistributedLock distributedLock = IdempotentUtils.tryLockWithRenewal(redisTemplate, lockKey, 30);
        if (distributedLock == null) {
            log.warn("获取取消订单分布式锁失败，orderNo: {}", orderNo);
            throw new BusinessException(503, "系统繁忙，请稍后重试");
        }
        
        try {
            // 1. 查询订单
            Order order = orderMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                            .eq(Order::getOrderNo, orderNo)
                            .eq(Order::getIsDeleted, 0)
            );
            
            if (order == null) {
                log.warn("订单不存在，订单号: {}", orderNo);
                throw new BusinessException(404, "订单不存在");
            }
            
            // 2. 检查订单状态（只有待支付或已支付状态才能取消）
            OrderStatus currentStatus = OrderStatus.getByCode(order.getStatus());
            if (currentStatus == null) {
                log.error("订单状态异常，订单号: {}, 状态码: {}", orderNo, order.getStatus());
                throw new BusinessException(500, "订单状态异常");
            }
            
            // 检查是否可以取消
            if (!currentStatus.canTransitionTo(OrderStatus.CANCELLED)) {
                log.warn("订单状态不允许取消，订单号: {}, 当前状态: {}", orderNo, currentStatus.getDescription());
                throw new BusinessException(400, "订单状态不允许取消，当前状态: " + currentStatus.getDescription());
            }
            
            // 3. 检查订单是否已经取消
            if (currentStatus == OrderStatus.CANCELLED) {
                log.info("订单已经取消，订单号: {}", orderNo);
                return true;
            }
            
            // 4. 使用补偿服务取消订单（会更新状态并发布事件，触发库存回滚、优惠券释放等）
            String cancelReason = "用户主动取消订单";
            orderCompensationService.cancelOrder(order.getId(), orderNo, cancelReason);
            
            // 清除缓存（订单取消后，缓存需要更新）
            CacheUtils.delete(redisTemplate, "order:detail:" + orderNo);
            
            LogUtils.businessLog("ORDER_CANCEL", "订单取消成功", 
                    orderNo, order.getId(), order.getUserId(), currentStatus.getDescription());
            
            log.info("订单取消成功，订单号: {}, 订单ID: {}, 原状态: {}", 
                    orderNo, order.getId(), currentStatus.getDescription());
            
            return true;
        } finally {
            // 释放分布式锁（会自动停止续期任务）
            if (distributedLock != null) {
                distributedLock.unlock();
            }
        }
    }
}
