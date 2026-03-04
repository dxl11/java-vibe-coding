package com.vibe.coupon.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券创建DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class CouponCreateDTO {
    
    /**
     * 优惠券名称
     */
    @NotBlank(message = "优惠券名称不能为空")
    private String name;
    
    /**
     * 优惠券类型：1-满减券，2-折扣券，3-免邮券
     */
    @NotNull(message = "优惠券类型不能为空")
    private Integer type;
    
    /**
     * 优惠金额（满减券）
     */
    private BigDecimal discountAmount;
    
    /**
     * 折扣率（折扣券，0-100）
     */
    private BigDecimal discountRate;
    
    /**
     * 最低消费金额
     */
    private BigDecimal minAmount;
    
    /**
     * 发放总数
     */
    @NotNull(message = "发放总数不能为空")
    @Positive(message = "发放总数必须大于0")
    private Integer totalCount;
    
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    /**
     * 发放方式：1-手动发放，2-自动发放，3-活动发放
     */
    private Integer distributeType;
    
    /**
     * 每人限领数量
     */
    private Integer limitPerUser;
}
