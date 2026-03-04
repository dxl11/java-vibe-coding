package com.vibe.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.order.entity.OrderStateLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单状态流转记录Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface OrderStateLogMapper extends BaseMapper<OrderStateLog> {
}
