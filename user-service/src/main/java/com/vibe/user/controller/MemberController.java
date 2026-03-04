package com.vibe.user.controller;

import com.vibe.common.core.result.Result;
import com.vibe.user.dto.MemberDTO;
import com.vibe.user.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 会员控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/user/member")
public class MemberController {
    
    @Autowired
    private MemberService memberService;
    
    /**
     * 查询会员信息
     * 
     * @param userId 用户ID
     * @return 响应结果
     */
    @GetMapping("/{userId}")
    public Result<MemberDTO> getMemberByUserId(@PathVariable Long userId) {
        MemberDTO member = memberService.getMemberByUserId(userId);
        return Result.success(member);
    }
    
    /**
     * 升级会员等级
     * 
     * @param userId 用户ID
     * @param level 等级
     * @return 响应结果
     */
    @PostMapping("/{userId}/upgrade")
    public Result<Boolean> upgradeLevel(
            @PathVariable Long userId,
            @RequestParam Integer level) {
        boolean result = memberService.upgradeLevel(userId, level);
        return Result.success("会员升级成功", result);
    }
}
