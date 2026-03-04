package com.vibe.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.coupon.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户优惠券Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    
    /**
     * 查询用户已领取的优惠券数量
     * 
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId} " +
            "AND status = 0 AND is_deleted = 0")
    int countUserCoupons(@Param("userId") Long userId, @Param("couponId") Long couponId);
    
    /**
     * 查询用户可用优惠券列表
     * 
     * @param userId 用户ID
     * @return 用户优惠券列表
     */
    @Select("SELECT uc.* FROM user_coupon uc " +
            "INNER JOIN coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} AND uc.status = 0 AND uc.is_deleted = 0 " +
            "AND c.status = 1 AND c.start_time <= NOW() AND c.end_time >= NOW() " +
            "ORDER BY c.end_time ASC")
    List<UserCoupon> selectAvailableCoupons(@Param("userId") Long userId);
    
    /**
     * 更新优惠券为已使用
     * 
     * @param id 用户优惠券ID
     * @param orderId 订单ID
     * @return 更新行数
     */
    @Update("UPDATE user_coupon SET status = 1, use_time = NOW(), order_id = #{orderId}, update_time = NOW() " +
            "WHERE id = #{id} AND status = 0 AND is_deleted = 0")
    int markAsUsed(@Param("id") Long id, @Param("orderId") Long orderId);
}
