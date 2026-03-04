package com.vibe.user.service;

import com.vibe.user.dto.PointRecordDTO;

import java.util.List;

/**
 * 积分服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface PointService {
    
    /**
     * 增加积分
     * 
     * @param userId 用户ID
     * @param points 积分数量
     * @param source 来源
     * @param sourceId 来源ID
     * @param remark 备注
     * @return 是否成功
     */
    boolean addPoints(Long userId, Integer points, String source, Long sourceId, String remark);
    
    /**
     * 消费积分
     * 
     * @param userId 用户ID
     * @param points 积分数量
     * @param source 来源
     * @param sourceId 来源ID
     * @param remark 备注
     * @return 是否成功
     */
    boolean consumePoints(Long userId, Integer points, String source, Long sourceId, String remark);
    
    /**
     * 查询积分记录
     * 
     * @param userId 用户ID
     * @param pointType 积分类型（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 积分记录列表
     */
    List<PointRecordDTO> getPointRecords(Long userId, Integer pointType, Integer page, Integer size);
    
    /**
     * 查询用户当前积分
     * 
     * @param userId 用户ID
     * @return 积分数量
     */
    Integer getCurrentPoints(Long userId);
}
