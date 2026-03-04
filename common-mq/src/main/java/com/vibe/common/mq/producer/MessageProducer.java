package com.vibe.common.mq.producer;

import com.vibe.common.mq.dto.MessageDTO;

/**
 * 消息生产者接口
 * 定义消息发送的统一接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface MessageProducer {
    
    /**
     * 发送消息
     * 
     * @param topic Topic 名称
     * @param messageDTO 消息DTO
     * @return 消息ID
     */
    String sendMessage(String topic, MessageDTO messageDTO);
    
    /**
     * 发送消息（带 Tag）
     * 
     * @param topic Topic 名称（业务主题，例如订单、库存等）
     * @param tag   消息标签（用于 RocketMQ 过滤与业务子类型区分）
     * @param messageDTO 消息DTO
     * @return 消息ID
     */
    String sendMessage(String topic, String tag, MessageDTO messageDTO);

    
    /**
     * 同步发送消息
     * 发送后等待服务器响应，适用于对可靠性要求高的场景
     * 
     * @param topic Topic 名称
     * @param messageDTO 消息DTO
     * @return 消息ID
     */
    String sendSyncMessage(String topic, MessageDTO messageDTO);

    /**
     * 同步发送消息（带 Tag）
     * 发送后等待服务器响应，适用于对可靠性要求高的场景
     *
     * @param topic Topic 名称（业务主题）
     * @param tag   消息标签（用于 RocketMQ 过滤与业务子类型区分）
     * @param messageDTO 消息DTO
     * @return 消息ID
     */
    String sendSyncMessage(String topic, String tag, MessageDTO messageDTO);
    
    /**
     * 异步发送消息
     * 发送后立即返回，通过回调处理结果，适用于对性能要求高的场景
     * 
     * @param topic Topic 名称
     * @param messageDTO 消息DTO
     * @return 消息ID
     */
    String sendAsyncMessage(String topic, MessageDTO messageDTO);

    /**
     * 异步发送消息（带 Tag）
     * 发送后立即返回，通过回调处理结果，适用于对性能要求高的场景
     *
     * @param topic Topic 名称（业务主题）
     * @param tag   消息标签（用于 RocketMQ 过滤与业务子类型区分）
     * @param messageDTO 消息DTO
     * @return 消息ID
     */
    String sendAsyncMessage(String topic, String tag, MessageDTO messageDTO);

    /**
     * 发送顺序消息
     * 保证同一消息队列中的消息有序（例如同一订单、同一账户的事件）
     *
     * @param topic Topic 名称（业务主题）
     * @param tag   消息标签（用于 RocketMQ 过滤与业务子类型区分）
     * @param messageDTO 消息DTO
     * @param orderKey 顺序键（用于路由到同一队列，通常使用业务主键，例如订单号）
     * @return 消息ID
     */
    String sendOrderlyMessage(String topic, String tag, MessageDTO messageDTO, String orderKey);

    /**
     * 发送延时消息
     *
     * @param topic Topic 名称（业务主题）
     * @param tag   消息标签（用于 RocketMQ 过滤与业务子类型区分）
     * @param messageDTO 消息DTO
     * @param delayLevel 延时级别（1-18，对应 RocketMQ 预设的延时时间）
     * @return 消息ID
     */
    String sendDelayMessage(String topic, String tag, MessageDTO messageDTO, int delayLevel);
}
