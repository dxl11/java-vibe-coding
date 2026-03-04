package com.vibe.common.mq.service.impl;

import com.vibe.common.mq.entity.LocalMessage;
import com.vibe.common.mq.mapper.LocalMessageMapper;
import com.vibe.common.mq.service.LocalMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 本地消息服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class LocalMessageServiceImpl implements LocalMessageService {
    
    @Autowired
    private LocalMessageMapper localMessageMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMessage(LocalMessage localMessage) {
        try {
            // 设置默认值
            if (localMessage.getStatus() == null) {
                localMessage.setStatus(LocalMessage.MessageStatus.PENDING.getCode());
            }
            if (localMessage.getRetryCount() == null) {
                localMessage.setRetryCount(0);
            }
            if (localMessage.getMaxRetryCount() == null) {
                localMessage.setMaxRetryCount(3);
            }
            if (localMessage.getIsDeleted() == null) {
                localMessage.setIsDeleted(0);
            }
            
            int result = localMessageMapper.insert(localMessage);
            return result > 0;
            
        } catch (Exception e) {
            log.error("保存本地消息失败，MessageId: {}", localMessage.getMessageId(), e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(String messageId, Integer status) {
        try {
            int result = localMessageMapper.updateStatus(messageId, status);
            return result > 0;
        } catch (Exception e) {
            log.error("更新消息状态失败，MessageId: {}, Status: {}", messageId, status, e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementRetryCount(String messageId) {
        try {
            int result = localMessageMapper.incrementRetryCount(messageId);
            return result > 0;
        } catch (Exception e) {
            log.error("增加重试次数失败，MessageId: {}", messageId, e);
            return false;
        }
    }
    
    @Override
    public List<LocalMessage> getPendingMessages(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;  // 默认查询100条
        }
        return localMessageMapper.selectPendingMessages(limit);
    }
    
    @Override
    public List<LocalMessage> getRetryMessages(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;  // 默认查询100条
        }
        return localMessageMapper.selectRetryMessages(java.time.LocalDateTime.now(), limit);
    }
    
    @Override
    public LocalMessage getByMessageId(String messageId) {
        return localMessageMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LocalMessage>()
                        .eq(LocalMessage::getMessageId, messageId)
                        .eq(LocalMessage::getIsDeleted, 0)
        );
    }
}
