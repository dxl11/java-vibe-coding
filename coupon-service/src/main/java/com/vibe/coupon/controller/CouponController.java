package com.vibe.coupon.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vibe.common.core.result.Result;
import com.vibe.coupon.dto.CouponCreateDTO;
import com.vibe.coupon.dto.CouponDTO;
import com.vibe.coupon.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/coupon")
public class CouponController {
    
    @Autowired
    private CouponService couponService;
    
    /**
     * 创建优惠券
     * 
     * @param createDTO 创建DTO
     * @return 响应结果
     */
    @PostMapping("/create")
    public Result<CouponDTO> createCoupon(@Validated @RequestBody CouponCreateDTO createDTO) {
        CouponDTO coupon = couponService.createCoupon(createDTO);
        return Result.success("优惠券创建成功", coupon);
    }
    
    /**
     * 根据ID查询优惠券
     * 
     * @param id 优惠券ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Result<CouponDTO> getCouponById(@PathVariable Long id) {
        CouponDTO coupon = couponService.getCouponById(id);
        return Result.success(coupon);
    }
    
    /**
     * 分页查询优惠券
     * 
     * @param page 页码
     * @param size 每页大小
     * @param status 状态（可选）
     * @return 响应结果
     */
    @GetMapping("/list")
    public Result<Page<CouponDTO>> listCoupons(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<CouponDTO> couponPage = couponService.listCoupons(page, size, status);
        return Result.success(couponPage);
    }
    
    /**
     * 查询可用优惠券列表
     * 
     * @return 响应结果
     */
    @GetMapping("/available")
    public Result<List<CouponDTO>> listAvailableCoupons() {
        List<CouponDTO> coupons = couponService.listAvailableCoupons();
        return Result.success(coupons);
    }
    
    /**
     * 发放优惠券
     * 
     * @param couponId 优惠券ID
     * @param userIds 用户ID列表
     * @return 响应结果
     */
    @PostMapping("/{couponId}/distribute")
    public Result<Integer> distributeCoupon(
            @PathVariable Long couponId,
            @RequestBody List<Long> userIds) {
        int count = couponService.distributeCoupon(couponId, userIds);
        return Result.success("发放优惠券完成", count);
    }
    
    /**
     * 启用/禁用优惠券
     * 
     * @param id 优惠券ID
     * @param enabled 是否启用
     * @return 响应结果
     */
    @PostMapping("/{id}/toggle")
    public Result<Boolean> toggleCouponStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        boolean result = couponService.toggleCouponStatus(id, enabled);
        return Result.success(enabled ? "优惠券已启用" : "优惠券已禁用", result);
    }
    
    /**
     * 计算优惠金额
     * 
     * @param couponId 优惠券ID
     * @param orderAmount 订单金额
     * @return 响应结果
     */
    @GetMapping("/{couponId}/calculate")
    public Result<BigDecimal> calculateDiscount(
            @PathVariable Long couponId,
            @RequestParam BigDecimal orderAmount) {
        BigDecimal discount = couponService.calculateDiscount(couponId, orderAmount);
        return Result.success(discount);
    }
}
