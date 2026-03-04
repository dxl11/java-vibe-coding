package com.vibe.coupon.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class CouponDTO {
    
    /**
     * 优惠券ID
     */
    private Long id;
    
    /**
     * 优惠券名称
     */
    private String name;
    
    /**
     * 优惠券类型：1-满减券，2-折扣券，3-免邮券
     */
    private Integer type;
    
    /**
     * 类型描述
     */
    private String typeDesc;
    
    /**
     * 优惠金额（满减券）
     */
    private BigDecimal discountAmount;
    
    /**
     * 折扣率（折扣券）
     */
    private BigDecimal discountRate;
    
    /**
     * 最低消费金额
     */
    private BigDecimal minAmount;
    
    /**
     * 发放总数
     */
    private Integer totalCount;
    
    /**
     * 已使用数量
     */
    private Integer usedCount;
    
    /**
     * 已领取数量
     */
    private Integer receivedCount;
    
    /**
     * 剩余数量
     */
    private Integer remainingCount;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 发放方式：1-手动发放，2-自动发放，3-活动发放
     */
    private Integer distributeType;
    
    /**
     * 每人限领数量
     */
    private Integer limitPerUser;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
