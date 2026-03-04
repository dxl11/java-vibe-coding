package com.vibe.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
    
    /**
     * 待支付
     */
    PENDING_PAYMENT(0, "待支付"),
    
    /**
     * 已支付
     */
    PAID(1, "已支付"),
    
    /**
     * 已发货
     */
    SHIPPED(2, "已发货"),
    
    /**
     * 已完成
     */
    COMPLETED(3, "已完成"),
    
    /**
     * 已取消
     */
    CANCELLED(4, "已取消"),
    
    /**
     * 创建失败
     */
    CREATION_FAILED(5, "创建失败"),
    
    /**
     * 支付失败
     */
    PAYMENT_FAILED(6, "支付失败");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 根据状态码获取枚举
     * 
     * @param code 状态码
     * @return 订单状态枚举
     */
    public static OrderStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否可以转换到目标状态
     * 
     * @param targetStatus 目标状态
     * @return 是否可以转换
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        // 定义状态转换规则
        switch (this) {
            case PENDING_PAYMENT:
                return targetStatus == PAID || targetStatus == CANCELLED || targetStatus == PAYMENT_FAILED;
            case PAID:
                return targetStatus == SHIPPED || targetStatus == CANCELLED;
            case SHIPPED:
                return targetStatus == COMPLETED;
            case CREATION_FAILED:
            case PAYMENT_FAILED:
            case CANCELLED:
            case COMPLETED:
                return false;  // 终态，不允许转换
            default:
                return false;
        }
    }
}
