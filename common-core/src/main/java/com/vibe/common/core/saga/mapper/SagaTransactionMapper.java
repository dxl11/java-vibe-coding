package com.vibe.common.core.saga.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.common.core.saga.entity.SagaTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SAGA 事务Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface SagaTransactionMapper extends BaseMapper<SagaTransaction> {
    
    /**
     * 更新事务状态
     * 
     * @param transactionId 事务ID
     * @param status 新状态
     * @return 更新行数
     */
    @Update("UPDATE saga_transaction SET status = #{status}, update_time = NOW() " +
            "WHERE transaction_id = #{transactionId} AND is_deleted = 0")
    int updateStatus(@Param("transactionId") String transactionId, @Param("status") Integer status);
    
    /**
     * 更新当前步骤
     * 
     * @param transactionId 事务ID
     * @param currentStep 当前步骤
     * @return 更新行数
     */
    @Update("UPDATE saga_transaction SET current_step = #{currentStep}, update_time = NOW() " +
            "WHERE transaction_id = #{transactionId} AND is_deleted = 0")
    int updateCurrentStep(@Param("transactionId") String transactionId, @Param("currentStep") String currentStep);
    
    /**
     * 查询超时的事务
     * 
     * @param currentTime 当前时间
     * @param limit 查询数量限制
     * @return 超时的事务列表
     */
    @Select("SELECT * FROM saga_transaction " +
            "WHERE status IN (0, 1) " +
            "AND expire_time IS NOT NULL " +
            "AND expire_time <= #{currentTime} " +
            "AND is_deleted = 0 " +
            "ORDER BY expire_time ASC " +
            "LIMIT #{limit}")
    List<SagaTransaction> selectTimeoutTransactions(@Param("currentTime") LocalDateTime currentTime, 
                                                     @Param("limit") Integer limit);
    
    /**
     * 根据业务ID查询事务
     * 
     * @param businessId 业务ID
     * @return 事务列表
     */
    @Select("SELECT * FROM saga_transaction " +
            "WHERE business_id = #{businessId} AND is_deleted = 0 " +
            "ORDER BY create_time DESC")
    List<SagaTransaction> selectByBusinessId(@Param("businessId") String businessId);
}
