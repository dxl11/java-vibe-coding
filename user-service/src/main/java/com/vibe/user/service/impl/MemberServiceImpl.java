package com.vibe.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.user.dto.MemberDTO;
import com.vibe.user.entity.Member;
import com.vibe.user.mapper.MemberMapper;
import com.vibe.user.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 会员服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class MemberServiceImpl implements MemberService {
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "MEMBER_INIT", resource = "MEMBER", action = "CREATE")
    @MonitorPerformance(threshold = 1000, operation = "初始化会员")
    public MemberDTO initMember(Long userId) {
        LogUtils.businessLog("MEMBER_INIT", "初始化会员开始", userId);
        
        // 检查是否已存在
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Member::getUserId, userId)
                .eq(Member::getIsDeleted, 0);
        Member existMember = memberMapper.selectOne(queryWrapper);
        
        if (existMember != null) {
            return convertToDTO(existMember);
        }
        
        // 创建会员（默认等级1）
        Member member = Member.builder()
                .userId(userId)
                .level(1)
                .levelName("普通会员")
                .points(0)
                .totalPoints(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        memberMapper.insert(member);
        
        LogUtils.businessLog("MEMBER_INIT", "初始化会员成功", member.getId());
        
        return convertToDTO(member);
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "会员查询")
    public MemberDTO getMemberByUserId(Long userId) {
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Member::getUserId, userId)
                .eq(Member::getIsDeleted, 0);
        
        Member member = memberMapper.selectOne(queryWrapper);
        if (member == null) {
            // 如果不存在，自动初始化
            return initMember(userId);
        }
        
        return convertToDTO(member);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "MEMBER_UPGRADE", resource = "MEMBER", action = "UPDATE")
    public boolean upgradeLevel(Long userId, Integer level) {
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getUserId, userId)
                        .eq(Member::getIsDeleted, 0));
        
        if (member == null) {
            throw new BusinessException(404, "会员信息不存在");
        }
        
        if (level <= member.getLevel()) {
            throw new BusinessException(400, "新等级必须大于当前等级");
        }
        
        // 根据等级设置等级名称
        String levelName = getLevelName(level);
        
        member.setLevel(level);
        member.setLevelName(levelName);
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.updateById(member);
        
        LogUtils.businessLog("MEMBER_UPGRADE", "会员升级成功", userId, level);
        return true;
    }
    
    /**
     * 获取等级名称
     * 
     * @param level 等级
     * @return 等级名称
     */
    private String getLevelName(Integer level) {
        switch (level) {
            case 1:
                return "普通会员";
            case 2:
                return "银牌会员";
            case 3:
                return "金牌会员";
            case 4:
                return "钻石会员";
            default:
                return "VIP" + level;
        }
    }
    
    /**
     * 转换为DTO
     * 
     * @param member 会员实体
     * @return 会员DTO
     */
    private MemberDTO convertToDTO(Member member) {
        MemberDTO dto = new MemberDTO();
        BeanUtils.copyProperties(member, dto);
        return dto;
    }
}
