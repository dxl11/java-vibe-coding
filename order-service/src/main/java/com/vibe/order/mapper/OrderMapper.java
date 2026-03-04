package com.vibe.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 使用版本号更新订单状态（乐观锁）
     *
     * @param orderId 订单ID
     * @param toStatus 目标状态码
     * @param version 当前版本号
     * @return 受影响行数
     */
    @Update("UPDATE `order` " +
            "SET status = #{toStatus}, update_time = NOW(), version = version + 1 " +
            "WHERE id = #{orderId} AND version = #{version} AND is_deleted = 0")
    int updateStatusWithVersion(@Param("orderId") Long orderId,
                                @Param("toStatus") Integer toStatus,
                                @Param("version") Integer version);
}
