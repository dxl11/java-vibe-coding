package com.vibe.coupon.service;

import com.vibe.coupon.dto.UserCouponDTO;

import java.util.List;

/**
 * 用户优惠券服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface UserCouponService {
    
    /**
     * 领取优惠券
     * 
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券DTO
     */
    UserCouponDTO receiveCoupon(Long userId, Long couponId);
    
    /**
     * 查询用户优惠券列表
     * 
     * @param userId 用户ID
     * @param status 状态（可选）
     * @return 用户优惠券列表
     */
    List<UserCouponDTO> getUserCoupons(Long userId, Integer status);
    
    /**
     * 查询用户可用优惠券列表
     * 
     * @param userId 用户ID
     * @param orderAmount 订单金额
     * @return 用户优惠券列表
     */
    List<UserCouponDTO> getAvailableCoupons(Long userId, java.math.BigDecimal orderAmount);
    
    /**
     * 使用优惠券
     * 
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean useCoupon(Long userCouponId, Long orderId);
    
    /**
     * 退还优惠券（订单取消时）
     * 
     * @param userCouponId 用户优惠券ID
     * @return 是否成功
     */
    boolean returnCoupon(Long userCouponId);
}
