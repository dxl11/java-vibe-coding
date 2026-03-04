package com.vibe.common.mq.service;

import com.vibe.common.mq.entity.LocalMessage;

import java.util.List;

/**
 * 本地消息服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface LocalMessageService {
    
    /**
     * 保存本地消息
     * 
     * @param localMessage 本地消息
     * @return 是否成功
     */
    boolean saveMessage(LocalMessage localMessage);
    
    /**
     * 更新消息状态
     * 
     * @param messageId 消息ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateStatus(String messageId, Integer status);
    
    /**
     * 增加重试次数
     * 
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean incrementRetryCount(String messageId);
    
    /**
     * 查询待发送的消息
     * 
     * @param limit 查询数量限制
     * @return 待发送的消息列表
     */
    List<LocalMessage> getPendingMessages(Integer limit);
    
    /**
     * 查询需要重试的消息
     * 
     * @param limit 查询数量限制
     * @return 需要重试的消息列表
     */
    List<LocalMessage> getRetryMessages(Integer limit);
    
    /**
     * 根据消息ID查询消息
     * 
     * @param messageId 消息ID
     * @return 本地消息
     */
    LocalMessage getByMessageId(String messageId);
}
