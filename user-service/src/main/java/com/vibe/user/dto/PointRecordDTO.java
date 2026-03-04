package com.vibe.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class PointRecordDTO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 积分类型：1-获得，2-消费
     */
    private Integer pointType;
    
    /**
     * 积分类型描述
     */
    private String pointTypeDesc;
    
    /**
     * 积分数量
     */
    private Integer points;
    
    /**
     * 变更前积分
     */
    private Integer beforePoints;
    
    /**
     * 变更后积分
     */
    private Integer afterPoints;
    
    /**
     * 来源：ORDER-订单，SIGN-签到，ACTIVITY-活动
     */
    private String source;
    
    /**
     * 来源ID
     */
    private Long sourceId;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
