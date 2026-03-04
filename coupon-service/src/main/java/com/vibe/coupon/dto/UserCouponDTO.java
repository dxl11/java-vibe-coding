package com.vibe.coupon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户优惠券DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class UserCouponDTO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 优惠券ID
     */
    private Long couponId;
    
    /**
     * 优惠券信息
     */
    private CouponDTO coupon;
    
    /**
     * 状态：0-未使用，1-已使用，2-已过期
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 使用时间
     */
    private LocalDateTime useTime;
    
    /**
     * 使用的订单ID
     */
    private Long orderId;
    
    /**
     * 领取时间
     */
    private LocalDateTime createTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}
