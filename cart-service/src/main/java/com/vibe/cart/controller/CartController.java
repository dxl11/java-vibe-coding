package com.vibe.cart.controller;

import com.vibe.common.core.result.Result;
import com.vibe.cart.dto.CartAddDTO;
import com.vibe.cart.dto.CartItemDTO;
import com.vibe.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    /**
     * 添加商品到购物车
     * 
     * @param userId 用户ID（可选）
     * @param tempUserId 临时用户ID（可选）
     * @param addDTO 添加DTO
     * @return 响应结果
     */
    @PostMapping("/add")
    public Result<CartItemDTO> addToCart(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String tempUserId,
            @Validated @RequestBody CartAddDTO addDTO) {
        CartItemDTO cartItem = cartService.addToCart(userId, tempUserId, addDTO);
        return Result.success("添加购物车成功", cartItem);
    }
    
    /**
     * 更新购物车项数量
     * 
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @param quantity 数量
     * @return 响应结果
     */
    @PutMapping("/{cartItemId}/quantity")
    public Result<CartItemDTO> updateQuantity(
            @RequestParam Long userId,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        CartItemDTO cartItem = cartService.updateQuantity(userId, cartItemId, quantity);
        return Result.success("更新数量成功", cartItem);
    }
    
    /**
     * 删除购物车项
     * 
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @return 响应结果
     */
    @DeleteMapping("/{cartItemId}")
    public Result<Boolean> deleteCartItem(
            @RequestParam Long userId,
            @PathVariable Long cartItemId) {
        boolean result = cartService.deleteCartItem(userId, cartItemId);
        return Result.success("删除成功", result);
    }
    
    /**
     * 批量删除购物车项
     * 
     * @param userId 用户ID
     * @param cartItemIds 购物车项ID列表
     * @return 响应结果
     */
    @PostMapping("/batch/delete")
    public Result<Integer> batchDeleteCartItems(
            @RequestParam Long userId,
            @RequestBody List<Long> cartItemIds) {
        int count = cartService.batchDeleteCartItems(userId, cartItemIds);
        return Result.success("批量删除完成", count);
    }
    
    /**
     * 查询购物车列表
     * 
     * @param userId 用户ID（可选）
     * @param tempUserId 临时用户ID（可选）
     * @return 响应结果
     */
    @GetMapping("/list")
    public Result<List<CartItemDTO>> getCartList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String tempUserId) {
        List<CartItemDTO> cartItems = cartService.getCartList(userId, tempUserId);
        return Result.success(cartItems);
    }
    
    /**
     * 合并购物车
     * 
     * @param userId 用户ID
     * @param tempUserId 临时用户ID
     * @return 响应结果
     */
    @PostMapping("/merge")
    public Result<Integer> mergeCart(
            @RequestParam Long userId,
            @RequestParam String tempUserId) {
        int count = cartService.mergeCart(userId, tempUserId);
        return Result.success("合并购物车完成", count);
    }
    
    /**
     * 清空购物车
     * 
     * @param userId 用户ID
     * @return 响应结果
     */
    @PostMapping("/clear")
    public Result<Boolean> clearCart(@RequestParam Long userId) {
        boolean result = cartService.clearCart(userId);
        return Result.success("清空购物车成功", result);
    }
    
    /**
     * 更新选中状态
     * 
     * @param userId 用户ID
     * @param selected 选中状态
     * @return 响应结果
     */
    @PutMapping("/selected")
    public Result<Boolean> updateSelected(
            @RequestParam Long userId,
            @RequestParam Integer selected) {
        boolean result = cartService.updateSelected(userId, selected);
        return Result.success("更新选中状态成功", result);
    }
}
