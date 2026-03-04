package com.vibe.cart.service;

import com.vibe.cart.dto.CartAddDTO;
import com.vibe.cart.dto.CartItemDTO;

import java.util.List;

/**
 * 购物车服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface CartService {
    
    /**
     * 添加商品到购物车
     * 
     * @param userId 用户ID（登录用户）
     * @param tempUserId 临时用户ID（未登录用户）
     * @param addDTO 添加DTO
     * @return 购物车项DTO
     */
    CartItemDTO addToCart(Long userId, String tempUserId, CartAddDTO addDTO);
    
    /**
     * 更新购物车项数量
     * 
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @param quantity 数量
     * @return 购物车项DTO
     */
    CartItemDTO updateQuantity(Long userId, Long cartItemId, Integer quantity);
    
    /**
     * 删除购物车项
     * 
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @return 是否成功
     */
    boolean deleteCartItem(Long userId, Long cartItemId);
    
    /**
     * 批量删除购物车项
     * 
     * @param userId 用户ID
     * @param cartItemIds 购物车项ID列表
     * @return 成功数量
     */
    int batchDeleteCartItems(Long userId, List<Long> cartItemIds);
    
    /**
     * 查询购物车列表
     * 
     * @param userId 用户ID（登录用户）
     * @param tempUserId 临时用户ID（未登录用户）
     * @return 购物车项列表
     */
    List<CartItemDTO> getCartList(Long userId, String tempUserId);
    
    /**
     * 合并购物车（登录后合并未登录购物车）
     * 
     * @param userId 用户ID
     * @param tempUserId 临时用户ID
     * @return 合并后的购物车项数量
     */
    int mergeCart(Long userId, String tempUserId);
    
    /**
     * 清空购物车
     * 
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearCart(Long userId);
    
    /**
     * 更新选中状态
     * 
     * @param userId 用户ID
     * @param selected 选中状态
     * @return 是否成功
     */
    boolean updateSelected(Long userId, Integer selected);
}
