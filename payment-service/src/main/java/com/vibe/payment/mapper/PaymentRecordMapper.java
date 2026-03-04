package com.vibe.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.payment.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 支付记录Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
    
    /**
     * 根据支付流水号查询
     * 
     * @param paymentNo 支付流水号
     * @return 支付记录
     */
    @Select("SELECT * FROM payment_record WHERE payment_no = #{paymentNo} AND is_deleted = 0")
    PaymentRecord selectByPaymentNo(@Param("paymentNo") String paymentNo);
    
    /**
     * 根据订单号查询
     * 
     * @param orderNo 订单号
     * @return 支付记录
     */
    @Select("SELECT * FROM payment_record WHERE order_no = #{orderNo} AND is_deleted = 0 ORDER BY create_time DESC LIMIT 1")
    PaymentRecord selectByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 更新支付状态
     * 
     * @param paymentNo 支付流水号
     * @param status 状态
     * @param thirdPartyPaymentNo 第三方支付流水号
     * @return 更新行数
     */
    @Update("UPDATE payment_record SET status = #{status}, third_party_payment_no = #{thirdPartyPaymentNo}, " +
            "pay_time = NOW(), update_time = NOW() WHERE payment_no = #{paymentNo} AND is_deleted = 0")
    int updatePaymentStatus(@Param("paymentNo") String paymentNo, 
                           @Param("status") Integer status,
                           @Param("thirdPartyPaymentNo") String thirdPartyPaymentNo);
}
