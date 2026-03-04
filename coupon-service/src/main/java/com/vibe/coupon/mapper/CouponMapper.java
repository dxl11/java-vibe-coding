package com.vibe.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.coupon.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 优惠券Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
    
    /**
     * 增加已领取数量
     * 
     * @param couponId 优惠券ID
     * @param count 数量
     * @return 更新行数
     */
    @Update("UPDATE coupon SET received_count = received_count + #{count}, update_time = NOW() " +
            "WHERE id = #{couponId} AND received_count + #{count} <= total_count AND is_deleted = 0")
    int increaseReceivedCount(@Param("couponId") Long couponId, @Param("count") Integer count);
    
    /**
     * 增加已使用数量
     * 
     * @param couponId 优惠券ID
     * @return 更新行数
     */
    @Update("UPDATE coupon SET used_count = used_count + 1, update_time = NOW() " +
            "WHERE id = #{couponId} AND is_deleted = 0")
    int increaseUsedCount(@Param("couponId") Long couponId);
}
