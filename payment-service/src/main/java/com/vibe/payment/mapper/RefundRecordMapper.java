package com.vibe.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.payment.entity.RefundRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 退款记录Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface RefundRecordMapper extends BaseMapper<RefundRecord> {
    
    /**
     * 根据退款流水号查询
     * 
     * @param refundNo 退款流水号
     * @return 退款记录
     */
    @Select("SELECT * FROM refund_record WHERE refund_no = #{refundNo} AND is_deleted = 0")
    RefundRecord selectByRefundNo(@Param("refundNo") String refundNo);
    
    /**
     * 根据支付流水号查询退款记录列表
     * 
     * @param paymentNo 支付流水号
     * @return 退款记录列表
     */
    @Select("SELECT * FROM refund_record WHERE payment_no = #{paymentNo} AND is_deleted = 0 ORDER BY create_time DESC")
    List<RefundRecord> selectByPaymentNo(@Param("paymentNo") String paymentNo);
    
    /**
     * 更新退款状态
     * 
     * @param refundNo 退款流水号
     * @param status 状态
     * @param thirdPartyRefundNo 第三方退款流水号
     * @return 更新行数
     */
    @Update("UPDATE refund_record SET status = #{status}, third_party_refund_no = #{thirdPartyRefundNo}, " +
            "refund_time = NOW(), update_time = NOW() WHERE refund_no = #{refundNo} AND is_deleted = 0")
    int updateRefundStatus(@Param("refundNo") String refundNo, 
                          @Param("status") Integer status,
                          @Param("thirdPartyRefundNo") String thirdPartyRefundNo);
}
