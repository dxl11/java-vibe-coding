package com.vibe.common.mq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.common.mq.entity.LocalMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface LocalMessageMapper extends BaseMapper<LocalMessage> {
    
    /**
     * 更新消息状态
     * 
     * @param messageId 消息ID
     * @param status 新状态
     * @return 更新行数
     */
    @Update("UPDATE local_message SET status = #{status}, update_time = NOW() " +
            "WHERE message_id = #{messageId} AND is_deleted = 0")
    int updateStatus(@Param("messageId") String messageId, @Param("status") Integer status);
    
    /**
     * 增加重试次数
     * 
     * @param messageId 消息ID
     * @return 更新行数
     */
    @Update("UPDATE local_message SET retry_count = retry_count + 1, " +
            "next_retry_time = DATE_ADD(NOW(), INTERVAL 1 MINUTE), " +
            "update_time = NOW() " +
            "WHERE message_id = #{messageId} AND is_deleted = 0")
    int incrementRetryCount(@Param("messageId") String messageId);
    
    /**
     * 查询待发送的消息
     * 
     * @param limit 查询数量限制
     * @return 待发送的消息列表
     */
    List<LocalMessage> selectPendingMessages(@Param("limit") Integer limit);
    
    /**
     * 查询需要重试的消息
     * 
     * @param currentTime 当前时间
     * @param limit 查询数量限制
     * @return 需要重试的消息列表
     */
    List<LocalMessage> selectRetryMessages(@Param("currentTime") LocalDateTime currentTime, 
                                          @Param("limit") Integer limit);
}
