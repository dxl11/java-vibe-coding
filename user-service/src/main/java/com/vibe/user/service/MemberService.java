package com.vibe.user.service;

import com.vibe.user.dto.MemberDTO;

/**
 * 会员服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface MemberService {
    
    /**
     * 初始化会员信息
     * 
     * @param userId 用户ID
     * @return 会员DTO
     */
    MemberDTO initMember(Long userId);
    
    /**
     * 查询会员信息
     * 
     * @param userId 用户ID
     * @return 会员DTO
     */
    MemberDTO getMemberByUserId(Long userId);
    
    /**
     * 升级会员等级
     * 
     * @param userId 用户ID
     * @param level 等级
     * @return 是否成功
     */
    boolean upgradeLevel(Long userId, Integer level);
}
