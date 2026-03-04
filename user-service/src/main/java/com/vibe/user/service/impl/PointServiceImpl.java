package com.vibe.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.user.dto.PointRecordDTO;
import com.vibe.user.entity.Member;
import com.vibe.user.entity.PointRecord;
import com.vibe.user.enums.PointType;
import com.vibe.user.mapper.MemberMapper;
import com.vibe.user.mapper.PointRecordMapper;
import com.vibe.user.service.MemberService;
import com.vibe.user.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class PointServiceImpl implements PointService {
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Autowired
    private PointRecordMapper pointRecordMapper;
    
    @Autowired
    private MemberService memberService;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "POINT_ADD", resource = "POINT", action = "UPDATE")
    @MonitorPerformance(threshold = 1000, operation = "增加积分")
    public boolean addPoints(Long userId, Integer points, String source, Long sourceId, String remark) {
        LogUtils.businessLog("POINT_ADD", "增加积分开始", userId, points, source);
        
        // 确保会员信息存在
        memberService.initMember(userId);
        
        // 查询当前积分
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getUserId, userId)
                        .eq(Member::getIsDeleted, 0));
        
        int beforePoints = member.getPoints();
        int afterPoints = beforePoints + points;
        
        // 增加积分
        int result = memberMapper.increasePoints(userId, points);
        if (result <= 0) {
            throw new BusinessException(400, "增加积分失败");
        }
        
        // 记录积分记录
        PointRecord pointRecord = PointRecord.builder()
                .userId(userId)
                .pointType(PointType.EARN.getCode())
                .points(points)
                .beforePoints(beforePoints)
                .afterPoints(afterPoints)
                .source(source)
                .sourceId(sourceId)
                .remark(remark)
                .createTime(LocalDateTime.now())
                .build();
        
        pointRecordMapper.insert(pointRecord);
        
        LogUtils.businessLog("POINT_ADD", "增加积分成功", userId, points, afterPoints);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "POINT_CONSUME", resource = "POINT", action = "UPDATE")
    @MonitorPerformance(threshold = 1000, operation = "消费积分")
    public boolean consumePoints(Long userId, Integer points, String source, Long sourceId, String remark) {
        LogUtils.businessLog("POINT_CONSUME", "消费积分开始", userId, points, source);
        
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getUserId, userId)
                        .eq(Member::getIsDeleted, 0));
        
        if (member == null) {
            throw new BusinessException(404, "会员信息不存在");
        }
        
        if (member.getPoints() < points) {
            throw new BusinessException(400, "积分不足");
        }
        
        int beforePoints = member.getPoints();
        int afterPoints = beforePoints - points;
        
        // 消费积分
        int result = memberMapper.consumePoints(userId, points);
        if (result <= 0) {
            throw new BusinessException(400, "消费积分失败，积分不足");
        }
        
        // 记录积分记录
        PointRecord pointRecord = PointRecord.builder()
                .userId(userId)
                .pointType(PointType.CONSUME.getCode())
                .points(points)
                .beforePoints(beforePoints)
                .afterPoints(afterPoints)
                .source(source)
                .sourceId(sourceId)
                .remark(remark)
                .createTime(LocalDateTime.now())
                .build();
        
        pointRecordMapper.insert(pointRecord);
        
        LogUtils.businessLog("POINT_CONSUME", "消费积分成功", userId, points, afterPoints);
        return true;
    }
    
    @Override
    @MonitorPerformance(threshold = 1000, operation = "积分记录查询")
    public List<PointRecordDTO> getPointRecords(Long userId, Integer pointType, Integer page, Integer size) {
        LambdaQueryWrapper<PointRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointRecord::getUserId, userId);
        
        if (pointType != null) {
            queryWrapper.eq(PointRecord::getPointType, pointType);
        }
        
        queryWrapper.orderByDesc(PointRecord::getCreateTime)
                .last("LIMIT " + (page - 1) * size + ", " + size);
        
        List<PointRecord> records = pointRecordMapper.selectList(queryWrapper);
        
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "当前积分查询")
    public Integer getCurrentPoints(Long userId) {
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getUserId, userId)
                        .eq(Member::getIsDeleted, 0));
        
        if (member == null) {
            return 0;
        }
        
        return member.getPoints();
    }
    
    /**
     * 转换为DTO
     * 
     * @param pointRecord 积分记录实体
     * @return 积分记录DTO
     */
    private PointRecordDTO convertToDTO(PointRecord pointRecord) {
        PointRecordDTO dto = new PointRecordDTO();
        BeanUtils.copyProperties(pointRecord, dto);
        
        PointType type = PointType.getByCode(pointRecord.getPointType());
        if (type != null) {
            dto.setPointTypeDesc(type.getDescription());
        }
        
        return dto;
    }
}
