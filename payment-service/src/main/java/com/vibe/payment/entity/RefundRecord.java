package com.vibe.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("refund_record")
public class RefundRecord {
    
    /**
     * 退款记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 支付记录ID
     */
    private Long paymentId;
    
    /**
     * 支付流水号
     */
    private String paymentNo;
    
    /**
     * 退款流水号
     */
    private String refundNo;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 退款状态：0-处理中，1-退款成功，2-退款失败
     */
    private Integer status;
    
    /**
     * 退款时间
     */
    private LocalDateTime refundTime;
    
    /**
     * 第三方退款流水号
     */
    private String thirdPartyRefundNo;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    private Integer isDeleted;
}
