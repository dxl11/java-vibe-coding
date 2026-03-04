package com.vibe.coupon.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vibe.common.core.log.LogUtils;
import com.vibe.coupon.entity.UserCoupon;
import com.vibe.coupon.enums.UserCouponStatus;
import com.vibe.coupon.mapper.CouponMapper;
import com.vibe.coupon.mapper.UserCouponMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券过期处理任务
 * 定时检查并标记过期的优惠券
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class CouponExpireTask {
    
    @Autowired
    private UserCouponMapper userCouponMapper;
    
    @Autowired
    private CouponMapper couponMapper;
    
    /**
     * 处理过期优惠券
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000)
    @Transactional(rollbackFor = Exception.class)
    public void expireCoupons() {
        log.info("开始处理过期优惠券");
        LogUtils.businessLog("COUPON_EXPIRE", "处理过期优惠券开始", LocalDateTime.now());
        
        LocalDateTime now = LocalDateTime.now();
        
        // 查询所有未使用且优惠券已过期的用户优惠券
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getStatus, UserCouponStatus.UNUSED.getCode())
                .eq(UserCoupon::getIsDeleted, 0);
        
        List<UserCoupon> userCoupons = userCouponMapper.selectList(queryWrapper);
        
        int expireCount = 0;
        for (UserCoupon userCoupon : userCoupons) {
            try {
                // 查询优惠券信息
                com.vibe.coupon.entity.Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
                if (coupon != null && now.isAfter(coupon.getEndTime())) {
                    // 标记为已过期
                    LambdaUpdateWrapper<UserCoupon> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(UserCoupon::getId, userCoupon.getId())
                            .set(UserCoupon::getStatus, UserCouponStatus.EXPIRED.getCode())
                            .set(UserCoupon::getUpdateTime, now);
                    
                    userCouponMapper.update(null, updateWrapper);
                    expireCount++;
                }
            } catch (Exception e) {
                log.error("处理过期优惠券失败，用户优惠券ID: {}", userCoupon.getId(), e);
            }
        }
        
        log.info("过期优惠券处理完成，过期数量: {}", expireCount);
        LogUtils.businessLog("COUPON_EXPIRE", "处理过期优惠券完成", expireCount);
    }
}
