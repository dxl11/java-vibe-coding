package com.vibe.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.idempotent.IdempotentUtils;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.common.core.util.MoneyUtils;
import com.vibe.coupon.dto.CouponDTO;
import com.vibe.coupon.dto.UserCouponDTO;
import com.vibe.coupon.entity.Coupon;
import com.vibe.coupon.entity.UserCoupon;
import com.vibe.coupon.enums.UserCouponStatus;
import com.vibe.coupon.mapper.CouponMapper;
import com.vibe.coupon.mapper.UserCouponMapper;
import com.vibe.coupon.service.CouponService;
import com.vibe.coupon.service.UserCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户优惠券服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class UserCouponServiceImpl implements UserCouponService {
    
    @Autowired
    private UserCouponMapper userCouponMapper;
    
    @Autowired
    private CouponMapper couponMapper;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "COUPON_RECEIVE", resource = "USER_COUPON", action = "CREATE")
    @MonitorPerformance(threshold = 1000, operation = "领取优惠券")
    public UserCouponDTO receiveCoupon(Long userId, Long couponId) {
        LogUtils.businessLog("COUPON_RECEIVE", "领取优惠券开始", userId, couponId);
        
        // 防刷检查
        String antiSpamKey = "coupon:receive:antispam:" + userId + ":" + couponId;
        boolean canReceive = IdempotentUtils.checkAndSet(redisTemplate, antiSpamKey, "1", 60);
        if (!canReceive) {
            log.warn("领取优惠券过于频繁，userId: {}, couponId: {}", userId, couponId);
            throw new BusinessException(429, "领取过于频繁，请稍后再试");
        }
        
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BusinessException(404, "优惠券不存在");
        }
        
        // 检查优惠券状态
        if (coupon.getStatus() == 0) {
            throw new BusinessException(400, "优惠券已禁用");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new BusinessException(400, "优惠券不在有效期内");
        }
        
        // 检查是否还有剩余数量
        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new BusinessException(400, "优惠券已领完");
        }
        
        // 检查用户是否已达到限领数量
        int userCouponCount = userCouponMapper.countUserCoupons(userId, couponId);
        if (userCouponCount >= coupon.getLimitPerUser()) {
            throw new BusinessException(400, "已达到限领数量");
        }
        
        // 增加已领取数量（乐观锁）
        int result = couponMapper.increaseReceivedCount(couponId, 1);
        if (result <= 0) {
            throw new BusinessException(400, "优惠券已领完");
        }
        
        // 创建用户优惠券记录
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(UserCouponStatus.UNUSED.getCode())
                .createTime(now)
                .updateTime(now)
                .isDeleted(0)
                .build();
        
        userCouponMapper.insert(userCoupon);
        
        LogUtils.businessLog("COUPON_RECEIVE", "领取优惠券成功", userId, couponId, userCoupon.getId());
        
        return convertToDTO(userCoupon, coupon);
    }
    
    @Override
    @MonitorPerformance(threshold = 1000, operation = "用户优惠券查询")
    public List<UserCouponDTO> getUserCoupons(Long userId, Integer status) {
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getIsDeleted, 0);
        
        if (status != null) {
            queryWrapper.eq(UserCoupon::getStatus, status);
        }
        
        queryWrapper.orderByDesc(UserCoupon::getCreateTime);
        
        List<UserCoupon> userCoupons = userCouponMapper.selectList(queryWrapper);
        
        return userCoupons.stream()
                .map(uc -> {
                    Coupon coupon = couponMapper.selectById(uc.getCouponId());
                    return convertToDTO(uc, coupon);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @MonitorPerformance(threshold = 1000, operation = "可用优惠券查询")
    public List<UserCouponDTO> getAvailableCoupons(Long userId, BigDecimal orderAmount) {
        List<UserCoupon> userCoupons = userCouponMapper.selectAvailableCoupons(userId);
        
        return userCoupons.stream()
                .map(uc -> {
                    Coupon coupon = couponMapper.selectById(uc.getCouponId());
                    UserCouponDTO dto = convertToDTO(uc, coupon);
                    
                    // 检查是否满足最低消费金额
                    if (orderAmount != null && coupon.getMinAmount() != null) {
                        if (orderAmount.compareTo(coupon.getMinAmount()) < 0) {
                            return null;  // 不满足最低消费，过滤掉
                        }
                    }
                    
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "COUPON_USE", resource = "USER_COUPON", action = "UPDATE")
    public boolean useCoupon(Long userCouponId, Long orderId) {
        LogUtils.businessLog("COUPON_USE", "使用优惠券开始", userCouponId, orderId);
        
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || userCoupon.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户优惠券不存在");
        }
        
        if (userCoupon.getStatus() != UserCouponStatus.UNUSED.getCode()) {
            throw new BusinessException(400, "优惠券已使用或已过期");
        }
        
        // 检查优惠券是否过期
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new BusinessException(404, "优惠券不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(coupon.getEndTime())) {
            // 标记为已过期
            userCoupon.setStatus(UserCouponStatus.EXPIRED.getCode());
            userCoupon.setUpdateTime(now);
            userCouponMapper.updateById(userCoupon);
            throw new BusinessException(400, "优惠券已过期");
        }
        
        // 标记为已使用
        int result = userCouponMapper.markAsUsed(userCouponId, orderId);
        if (result <= 0) {
            throw new BusinessException(400, "优惠券使用失败");
        }
        
        // 增加已使用数量
        couponMapper.increaseUsedCount(coupon.getId());
        
        LogUtils.businessLog("COUPON_USE", "使用优惠券成功", userCouponId, orderId);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "COUPON_RETURN", resource = "USER_COUPON", action = "UPDATE")
    public boolean returnCoupon(Long userCouponId) {
        LogUtils.businessLog("COUPON_RETURN", "退还优惠券开始", userCouponId);
        
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || userCoupon.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户优惠券不存在");
        }
        
        if (userCoupon.getStatus() != UserCouponStatus.USED.getCode()) {
            throw new BusinessException(400, "只有已使用的优惠券才能退还");
        }
        
        // 检查优惠券是否过期
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new BusinessException(404, "优惠券不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(coupon.getEndTime())) {
            // 已过期，不能退还
            throw new BusinessException(400, "优惠券已过期，不能退还");
        }
        
        // 退还优惠券
        userCoupon.setStatus(UserCouponStatus.UNUSED.getCode());
        userCoupon.setUseTime(null);
        userCoupon.setOrderId(null);
        userCoupon.setUpdateTime(now);
        userCouponMapper.updateById(userCoupon);
        
        // 减少已使用数量
        // TODO: 需要在CouponMapper中添加decreaseUsedCount方法
        
        LogUtils.businessLog("COUPON_RETURN", "退还优惠券成功", userCouponId);
        return true;
    }
    
    /**
     * 转换为DTO
     * 
     * @param userCoupon 用户优惠券实体
     * @param coupon 优惠券实体
     * @return 用户优惠券DTO
     */
    private UserCouponDTO convertToDTO(UserCoupon userCoupon, Coupon coupon) {
        UserCouponDTO dto = new UserCouponDTO();
        BeanUtils.copyProperties(userCoupon, dto);
        
        UserCouponStatus status = UserCouponStatus.getByCode(userCoupon.getStatus());
        if (status != null) {
            dto.setStatusDesc(status.getDescription());
        }
        
        if (coupon != null) {
            CouponDTO couponDTO = new CouponDTO();
            BeanUtils.copyProperties(coupon, couponDTO);
            dto.setCoupon(couponDTO);
            dto.setExpireTime(coupon.getEndTime());
        }
        
        return dto;
    }
}
