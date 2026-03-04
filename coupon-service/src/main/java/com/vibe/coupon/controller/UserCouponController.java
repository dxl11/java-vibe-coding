package com.vibe.coupon.controller;

import com.vibe.common.core.result.Result;
import com.vibe.coupon.dto.UserCouponDTO;
import com.vibe.coupon.service.UserCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户优惠券控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/coupon/user")
public class UserCouponController {
    
    @Autowired
    private UserCouponService userCouponService;
    
    /**
     * 领取优惠券
     * 
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 响应结果
     */
    @PostMapping("/{userId}/receive/{couponId}")
    public Result<UserCouponDTO> receiveCoupon(
            @PathVariable Long userId,
            @PathVariable Long couponId) {
        UserCouponDTO userCoupon = userCouponService.receiveCoupon(userId, couponId);
        return Result.success("优惠券领取成功", userCoupon);
    }
    
    /**
     * 查询用户优惠券列表
     * 
     * @param userId 用户ID
     * @param status 状态（可选）
     * @return 响应结果
     */
    @GetMapping("/{userId}/list")
    public Result<List<UserCouponDTO>> getUserCoupons(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer status) {
        List<UserCouponDTO> userCoupons = userCouponService.getUserCoupons(userId, status);
        return Result.success(userCoupons);
    }
    
    /**
     * 查询用户可用优惠券列表
     * 
     * @param userId 用户ID
     * @param orderAmount 订单金额（可选）
     * @return 响应结果
     */
    @GetMapping("/{userId}/available")
    public Result<List<UserCouponDTO>> getAvailableCoupons(
            @PathVariable Long userId,
            @RequestParam(required = false) BigDecimal orderAmount) {
        List<UserCouponDTO> userCoupons = userCouponService.getAvailableCoupons(userId, orderAmount);
        return Result.success(userCoupons);
    }
    
    /**
     * 使用优惠券
     * 
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     * @return 响应结果
     */
    @PostMapping("/{userCouponId}/use")
    public Result<Boolean> useCoupon(
            @PathVariable Long userCouponId,
            @RequestParam Long orderId) {
        boolean result = userCouponService.useCoupon(userCouponId, orderId);
        return Result.success("优惠券使用成功", result);
    }
    
    /**
     * 退还优惠券
     * 
     * @param userCouponId 用户优惠券ID
     * @return 响应结果
     */
    @PostMapping("/{userCouponId}/return")
    public Result<Boolean> returnCoupon(@PathVariable Long userCouponId) {
        boolean result = userCouponService.returnCoupon(userCouponId);
        return Result.success("优惠券退还成功", result);
    }
}
