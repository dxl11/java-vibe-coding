package com.vibe.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单状态流转记录实体类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_state_log")
public class OrderStateLog {
    
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 原状态
     */
    private Integer fromState;
    
    /**
     * 新状态
     */
    private Integer toState;
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 事件数据（JSON格式）
     */
    private String eventData;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
