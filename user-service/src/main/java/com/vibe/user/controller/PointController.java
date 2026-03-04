package com.vibe.user.controller;

import com.vibe.common.core.result.Result;
import com.vibe.user.dto.PointRecordDTO;
import com.vibe.user.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/user/point")
public class PointController {
    
    @Autowired
    private PointService pointService;
    
    /**
     * 查询积分记录
     * 
     * @param userId 用户ID
     * @param pointType 积分类型（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 响应结果
     */
    @GetMapping("/{userId}/records")
    public Result<List<PointRecordDTO>> getPointRecords(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer pointType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<PointRecordDTO> records = pointService.getPointRecords(userId, pointType, page, size);
        return Result.success(records);
    }
    
    /**
     * 查询当前积分
     * 
     * @param userId 用户ID
     * @return 响应结果
     */
    @GetMapping("/{userId}/current")
    public Result<Integer> getCurrentPoints(@PathVariable Long userId) {
        Integer points = pointService.getCurrentPoints(userId);
        return Result.success(points);
    }
}
