package com.vibe.common.mq.service;

import com.vibe.common.mq.entity.LocalMessage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 死信消息服务
 * 用于查询和人工重试本地消息表中发送失败的消息
 *
 * @author vibe
 * @date 2024-01-13
 */
public interface DeadLetterService {

    /**
     * 查询死信消息
     *
     * @param topic      Topic 名称（可选）
     * @param businessId 业务ID（可选）
     * @param fromTime   起始时间（可选）
     * @param toTime     截止时间（可选）
     * @param limit      返回数量上限
     * @return 死信消息列表
     */
    List<LocalMessage> queryDeadLetters(String topic,
                                        String businessId,
                                        LocalDateTime fromTime,
                                        LocalDateTime toTime,
                                        Integer limit);

    /**
     * 将指定消息重新标记为待发送状态
     *
     * @param messageId 消息唯一标识
     * @return 是否成功
     */
    boolean retry(String messageId);
}

