package com.vibe.coupon.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vibe.coupon.dto.CouponCreateDTO;
import com.vibe.coupon.dto.CouponDTO;

import java.util.List;

/**
 * 优惠券服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface CouponService {
    
    /**
     * 创建优惠券
     * 
     * @param createDTO 创建DTO
     * @return 优惠券DTO
     */
    CouponDTO createCoupon(CouponCreateDTO createDTO);
    
    /**
     * 根据ID查询优惠券
     * 
     * @param id 优惠券ID
     * @return 优惠券DTO
     */
    CouponDTO getCouponById(Long id);
    
    /**
     * 分页查询优惠券
     * 
     * @param page 页码
     * @param size 每页大小
     * @param status 状态（可选）
     * @return 分页结果
     */
    Page<CouponDTO> listCoupons(Integer page, Integer size, Integer status);
    
    /**
     * 查询可用优惠券列表
     * 
     * @return 优惠券列表
     */
    List<CouponDTO> listAvailableCoupons();
    
    /**
     * 发放优惠券（手动发放）
     * 
     * @param couponId 优惠券ID
     * @param userIds 用户ID列表
     * @return 成功数量
     */
    int distributeCoupon(Long couponId, List<Long> userIds);
    
    /**
     * 启用/禁用优惠券
     * 
     * @param id 优惠券ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean toggleCouponStatus(Long id, boolean enabled);
    
    /**
     * 计算优惠金额
     * 
     * @param couponId 优惠券ID
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    java.math.BigDecimal calculateDiscount(Long couponId, java.math.BigDecimal orderAmount);
}
