package com.vibe.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class MemberDTO {
    
    /**
     * 会员ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会员等级
     */
    private Integer level;
    
    /**
     * 等级名称
     */
    private String levelName;
    
    /**
     * 积分
     */
    private Integer points;
    
    /**
     * 累计积分
     */
    private Integer totalPoints;
    
    /**
     * 会员过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
