package com.vibe.common.mq.consumer;

import com.vibe.common.mq.dto.MessageDTO;

/**
 * 消息消费者接口
 * 定义消息接收的统一接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface MessageConsumer {
    
    /**
     * 消费消息
     * 
     * @param messageDTO 消息DTO
     */
    void consume(MessageDTO messageDTO);
}
