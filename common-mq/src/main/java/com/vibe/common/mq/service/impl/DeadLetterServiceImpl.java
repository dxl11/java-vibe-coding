package com.vibe.common.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.mq.entity.LocalMessage;
import com.vibe.common.mq.mapper.LocalMessageMapper;
import com.vibe.common.mq.service.DeadLetterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 死信消息服务实现
 *
 * 仅提供基础查询与人工重试能力，供后续运维/管理端调用
 *
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class DeadLetterServiceImpl implements DeadLetterService {

    @Autowired
    private LocalMessageMapper localMessageMapper;

    @Override
    public List<LocalMessage> queryDeadLetters(String topic,
                                               String businessId,
                                               LocalDateTime fromTime,
                                               LocalDateTime toTime,
                                               Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        LambdaQueryWrapper<LocalMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocalMessage::getStatus, LocalMessage.MessageStatus.FAILED.getCode())
                .eq(LocalMessage::getIsDeleted, 0);

        if (topic != null && !topic.isEmpty()) {
            wrapper.eq(LocalMessage::getTopic, topic);
        }
        if (businessId != null && !businessId.isEmpty()) {
            wrapper.eq(LocalMessage::getBusinessId, businessId);
        }
        if (fromTime != null) {
            wrapper.ge(LocalMessage::getCreateTime, fromTime);
        }
        if (toTime != null) {
            wrapper.le(LocalMessage::getCreateTime, toTime);
        }

        wrapper.orderByDesc(LocalMessage::getCreateTime).last("LIMIT " + limit);
        return localMessageMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retry(String messageId) {
        try {
            LocalMessage message = localMessageMapper.selectOne(
                    new LambdaQueryWrapper<LocalMessage>()
                            .eq(LocalMessage::getMessageId, messageId)
                            .eq(LocalMessage::getIsDeleted, 0)
            );
            if (message == null) {
                log.warn("未找到需要重试的本地消息，messageId: {}", messageId);
                return false;
            }
            // 将状态恢复为待发送，重试次数不清零，便于后续观察
            message.setStatus(LocalMessage.MessageStatus.PENDING.getCode());
            message.setNextRetryTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            int updated = localMessageMapper.updateById(message);
            return updated > 0;
        } catch (Exception e) {
            log.error("重试死信消息失败，messageId: {}", messageId, e);
            return false;
        }
    }
}

