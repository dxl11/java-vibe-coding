package com.vibe.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 积分记录实体类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("point_record")
public class PointRecord {
    
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
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
     * 来源ID（如订单ID）
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
