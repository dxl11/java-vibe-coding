package com.vibe.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vibe.common.core.cache.CacheUtils;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.common.core.util.MoneyUtils;
import com.vibe.cart.dto.CartAddDTO;
import com.vibe.cart.dto.CartItemDTO;
import com.vibe.cart.entity.CartItem;
import com.vibe.cart.mapper.CartItemMapper;
import com.vibe.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartItemMapper cartItemMapper;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "CART_ADD", resource = "CART", action = "CREATE")
    @MonitorPerformance(threshold = 1000, operation = "添加购物车")
    public CartItemDTO addToCart(Long userId, String tempUserId, CartAddDTO addDTO) {
        LogUtils.businessLog("CART_ADD", "添加购物车开始", userId != null ? userId : tempUserId, addDTO.getProductId());
        
        // 参数校验
        if (userId == null && (tempUserId == null || tempUserId.isEmpty())) {
            throw new BusinessException(400, "用户ID或临时用户ID不能为空");
        }
        
        // TODO: 调用商品服务获取商品信息
        // 这里简化处理，实际应该调用商品服务API
        String productName = "商品名称";  // TODO: 从商品服务获取
        String productImage = "商品图片URL";  // TODO: 从商品服务获取
        java.math.BigDecimal productPrice = MoneyUtils.createMoney("100.00");  // TODO: 从商品服务获取
        
        // 检查购物车中是否已存在该商品
        CartItem existItem = null;
        if (userId != null) {
            existItem = cartItemMapper.selectByUserAndProduct(userId, addDTO.getProductId(), addDTO.getSkuId());
        } else {
            // 临时用户查询
            LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CartItem::getTempUserId, tempUserId)
                    .eq(CartItem::getProductId, addDTO.getProductId())
                    .eq(addDTO.getSkuId() != null, CartItem::getSkuId, addDTO.getSkuId())
                    .eq(CartItem::getIsDeleted, 0);
            existItem = cartItemMapper.selectOne(queryWrapper);
        }
        
        if (existItem != null) {
            // 已存在，更新数量
            int newQuantity = existItem.getQuantity() + addDTO.getQuantity();
            cartItemMapper.updateQuantity(existItem.getId(), newQuantity);
            
            LogUtils.businessLog("CART_ADD", "购物车商品数量更新", existItem.getId(), newQuantity);
            
            CartItem updatedItem = cartItemMapper.selectById(existItem.getId());
            return convertToDTO(updatedItem);
        } else {
            // 不存在，新增
            java.math.BigDecimal subtotal = MoneyUtils.multiply(productPrice, addDTO.getQuantity());
            
            CartItem cartItem = CartItem.builder()
                    .userId(userId)
                    .tempUserId(tempUserId)
                    .productId(addDTO.getProductId())
                    .skuId(addDTO.getSkuId())
                    .productName(productName)
                    .productImage(productImage)
                    .productPrice(productPrice)
                    .quantity(addDTO.getQuantity())
                    .subtotal(subtotal)
                    .selected(1)  // 默认选中
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .isDeleted(0)
                    .build();
            
            cartItemMapper.insert(cartItem);
            
            LogUtils.businessLog("CART_ADD", "添加购物车成功", cartItem.getId());
            
            return convertToDTO(cartItem);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "CART_UPDATE", resource = "CART", action = "UPDATE")
    @MonitorPerformance(threshold = 500, operation = "更新购物车数量")
    public CartItemDTO updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null || cartItem.getIsDeleted() == 1) {
            throw new BusinessException(404, "购物车项不存在");
        }
        
        // 验证用户权限
        if (userId != null && !userId.equals(cartItem.getUserId())) {
            throw new BusinessException(403, "无权限操作");
        }
        
        if (quantity <= 0) {
            throw new BusinessException(400, "数量必须大于0");
        }
        
        cartItemMapper.updateQuantity(cartItemId, quantity);
        
        CartItem updatedItem = cartItemMapper.selectById(cartItemId);
        return convertToDTO(updatedItem);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "CART_DELETE", resource = "CART", action = "DELETE")
    public boolean deleteCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null || cartItem.getIsDeleted() == 1) {
            throw new BusinessException(404, "购物车项不存在");
        }
        
        // 验证用户权限
        if (userId != null && !userId.equals(cartItem.getUserId())) {
            throw new BusinessException(403, "无权限操作");
        }
        
        cartItem.setIsDeleted(1);
        cartItem.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(cartItem);
        
        LogUtils.businessLog("CART_DELETE", "删除购物车项成功", cartItemId);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "CART_BATCH_DELETE", resource = "CART", action = "DELETE")
    public int batchDeleteCartItems(Long userId, List<Long> cartItemIds) {
        int successCount = 0;
        for (Long cartItemId : cartItemIds) {
            try {
                if (deleteCartItem(userId, cartItemId)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量删除购物车项失败，ID: {}", cartItemId, e);
            }
        }
        LogUtils.businessLog("CART_BATCH_DELETE", "批量删除购物车项完成", successCount, cartItemIds.size());
        return successCount;
    }
    
    @Override
    @MonitorPerformance(threshold = 1000, operation = "购物车查询")
    public List<CartItemDTO> getCartList(Long userId, String tempUserId) {
        List<CartItem> cartItems;
        
        if (userId != null) {
            cartItems = cartItemMapper.selectByUserId(userId);
        } else if (tempUserId != null && !tempUserId.isEmpty()) {
            cartItems = cartItemMapper.selectByTempUserId(tempUserId);
        } else {
            throw new BusinessException(400, "用户ID或临时用户ID不能为空");
        }
        
        return cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "CART_MERGE", resource = "CART", action = "UPDATE")
    @MonitorPerformance(threshold = 2000, operation = "合并购物车")
    public int mergeCart(Long userId, String tempUserId) {
        LogUtils.businessLog("CART_MERGE", "合并购物车开始", userId, tempUserId);
        
        if (tempUserId == null || tempUserId.isEmpty()) {
            return 0;
        }
        
        // 查询临时用户的购物车
        List<CartItem> tempCartItems = cartItemMapper.selectByTempUserId(tempUserId);
        if (tempCartItems.isEmpty()) {
            return 0;
        }
        
        // 查询用户的购物车
        List<CartItem> userCartItems = cartItemMapper.selectByUserId(userId);
        
        int mergeCount = 0;
        for (CartItem tempItem : tempCartItems) {
            // 检查用户购物车中是否已存在该商品
            CartItem existItem = cartItemMapper.selectByUserAndProduct(
                    userId, tempItem.getProductId(), tempItem.getSkuId());
            
            if (existItem != null) {
                // 已存在，合并数量
                int newQuantity = existItem.getQuantity() + tempItem.getQuantity();
                cartItemMapper.updateQuantity(existItem.getId(), newQuantity);
                
                // 删除临时购物车项
                tempItem.setIsDeleted(1);
                tempItem.setUpdateTime(LocalDateTime.now());
                cartItemMapper.updateById(tempItem);
            } else {
                // 不存在，更新用户ID
                tempItem.setUserId(userId);
                tempItem.setTempUserId(null);
                tempItem.setUpdateTime(LocalDateTime.now());
                cartItemMapper.updateById(tempItem);
            }
            
            mergeCount++;
        }
        
        LogUtils.businessLog("CART_MERGE", "合并购物车完成", userId, mergeCount);
        return mergeCount;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "CART_CLEAR", resource = "CART", action = "DELETE")
    public boolean clearCart(Long userId) {
        LambdaUpdateWrapper<CartItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CartItem::getUserId, userId)
                .eq(CartItem::getIsDeleted, 0)
                .set(CartItem::getIsDeleted, 1)
                .set(CartItem::getUpdateTime, LocalDateTime.now());
        
        cartItemMapper.update(null, updateWrapper);
        
        LogUtils.businessLog("CART_CLEAR", "清空购物车成功", userId);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "CART_UPDATE_SELECTED", resource = "CART", action = "UPDATE")
    public boolean updateSelected(Long userId, Integer selected) {
        cartItemMapper.updateSelectedByUserId(userId, selected);
        LogUtils.businessLog("CART_UPDATE_SELECTED", "更新选中状态成功", userId, selected);
        return true;
    }
    
    /**
     * 转换为DTO
     * 
     * @param cartItem 购物车项实体
     * @return 购物车项DTO
     */
    private CartItemDTO convertToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        BeanUtils.copyProperties(cartItem, dto);
        
        // TODO: 实时查询库存和价格
        // dto.setStock(productService.getStock(cartItem.getProductId()));
        // dto.setValid(validateCartItem(cartItem));
        
        return dto;
    }
}
