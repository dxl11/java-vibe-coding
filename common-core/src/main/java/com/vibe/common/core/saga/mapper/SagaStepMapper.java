package com.vibe.common.core.saga.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.common.core.saga.entity.SagaStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * SAGA 步骤Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface SagaStepMapper extends BaseMapper<SagaStep> {
    
    /**
     * 更新步骤状态
     * 
     * @param stepId 步骤ID
     * @param status 新状态
     * @return 更新行数
     */
    @Update("UPDATE saga_step SET status = #{status}, update_time = NOW() " +
            "WHERE step_id = #{stepId} AND is_deleted = 0")
    int updateStatus(@Param("stepId") String stepId, @Param("status") Integer status);
    
    /**
     * 更新步骤执行时间
     * 
     * @param stepId 步骤ID
     * @param executeTime 执行时间
     * @param durationMs 执行耗时（毫秒）
     * @return 更新行数
     */
    @Update("UPDATE saga_step SET execute_time = #{executeTime}, duration_ms = #{durationMs}, update_time = NOW() " +
            "WHERE step_id = #{stepId} AND is_deleted = 0")
    int updateExecuteTime(@Param("stepId") String stepId, 
                         @Param("executeTime") java.time.LocalDateTime executeTime, 
                         @Param("durationMs") Long durationMs);
    
    /**
     * 更新步骤补偿时间
     * 
     * @param stepId 步骤ID
     * @param compensateTime 补偿时间
     * @return 更新行数
     */
    @Update("UPDATE saga_step SET compensate_time = #{compensateTime}, update_time = NOW() " +
            "WHERE step_id = #{stepId} AND is_deleted = 0")
    int updateCompensateTime(@Param("stepId") String stepId, 
                             @Param("compensateTime") java.time.LocalDateTime compensateTime);
    
    /**
     * 根据事务ID查询步骤列表
     * 
     * @param transactionId 事务ID
     * @return 步骤列表
     */
    @Select("SELECT * FROM saga_step " +
            "WHERE transaction_id = #{transactionId} AND is_deleted = 0 " +
            "ORDER BY step_order ASC")
    List<SagaStep> selectByTransactionId(@Param("transactionId") String transactionId);
    
    /**
     * 根据事务ID和状态查询步骤列表
     * 
     * @param transactionId 事务ID
     * @param status 状态
     * @return 步骤列表
     */
    @Select("SELECT * FROM saga_step " +
            "WHERE transaction_id = #{transactionId} AND status = #{status} AND is_deleted = 0 " +
            "ORDER BY step_order ASC")
    List<SagaStep> selectByTransactionIdAndStatus(@Param("transactionId") String transactionId, 
                                                    @Param("status") Integer status);
    
    /**
     * 增加重试次数
     * 
     * @param stepId 步骤ID
     * @param nextRetryTime 下次重试时间
     * @return 更新行数
     */
    @Update("UPDATE saga_step SET retry_count = retry_count + 1, " +
            "next_retry_time = #{nextRetryTime}, update_time = NOW() " +
            "WHERE step_id = #{stepId} AND is_deleted = 0")
    int incrementRetryCount(@Param("stepId") String stepId, 
                           @Param("nextRetryTime") java.time.LocalDateTime nextRetryTime);
    
    /**
     * 查询需要重试的步骤
     * 
     * @param currentTime 当前时间
     * @param limit 查询数量限制
     * @return 需要重试的步骤列表
     */
    @Select("SELECT * FROM saga_step " +
            "WHERE status = 2 " +
            "AND retry_count < max_retry_count " +
            "AND (next_retry_time IS NULL OR next_retry_time <= #{currentTime}) " +
            "AND is_deleted = 0 " +
            "ORDER BY next_retry_time ASC, create_time ASC " +
            "LIMIT #{limit}")
    List<SagaStep> selectRetryableSteps(@Param("currentTime") java.time.LocalDateTime currentTime, 
                                        @Param("limit") Integer limit);
}
