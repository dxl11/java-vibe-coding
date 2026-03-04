package com.vibe.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.order.entity.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单明细Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    
    /**
     * 批量插入订单明细
     * 
     * @param orderItems 订单明细列表
     * @return 插入行数
     */
    @Insert("<script>" +
            "INSERT INTO order_item (order_id, product_id, product_name, product_price, quantity, subtotal, create_time, update_time, is_deleted) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.orderId}, #{item.productId}, #{item.productName}, #{item.productPrice}, #{item.quantity}, #{item.subtotal}, #{item.createTime}, #{item.updateTime}, #{item.isDeleted})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<OrderItem> orderItems);
}
