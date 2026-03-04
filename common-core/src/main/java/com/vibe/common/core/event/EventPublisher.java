package com.vibe.common.core.event;

import com.vibe.common.core.log.TraceIdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.common.mq.config.RocketMQTopicConfig;
import com.vibe.common.mq.dto.MessageDTO;
import com.vibe.common.mq.entity.LocalMessage;
import com.vibe.common.mq.producer.MessageProducer;
import com.vibe.common.mq.service.LocalMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 事件发布器
 * 负责发布领域事件到消息队列
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class EventPublisher {
    
    @Autowired
    private MessageProducer messageProducer;
    
    @Autowired
    private LocalMessageService localMessageService;
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * 发布事件
     * 
     * @param topic Topic名称
     * @param tag Tag名称
     * @param event 事件对象
     */
    public void publish(String topic, String tag, BaseEvent event) {
        try {
            // 设置事件基本信息
            if (event.getEventId() == null || event.getEventId().isEmpty()) {
                event.setEventId(UUID.randomUUID().toString().replace("-", ""));
            }
            if (event.getEventTime() == null) {
                event.setEventTime(LocalDateTime.now());
            }
            if (event.getTraceId() == null || event.getTraceId().isEmpty()) {
                event.setTraceId(TraceIdUtils.getTraceIdOrNull());
            }
            
            // 序列化事件数据
            String eventData = OBJECT_MAPPER.writeValueAsString(event);
            event.setEventData(eventData);
            
            // 构建消息DTO
            MessageDTO messageDTO = MessageDTO.builder()
                    .messageId(event.getEventId())
                    .messageType(event.getEventType())
                    .title(event.getEventType())
                    .content(eventData)
                    .source(event.getSource())
                    .tag(tag)
                    .key(event.getBusinessId())
                    .build();
            
            // 保存本地消息（outbox）
            LocalMessage localMessage = LocalMessage.builder()
                    .messageId(messageDTO.getMessageId())
                    .topic(topic)
                    .tag(tag)
                    .messageBody(eventData)
                    .messageType(messageDTO.getMessageType())
                    .businessId(event.getBusinessId())
                    .build();
            boolean saved = localMessageService.saveMessage(localMessage);
            if (!saved) {
                log.error("保存本地消息失败，MessageId: {}, Topic: {}, Tag: {}", 
                        messageDTO.getMessageId(), topic, tag);
                throw new RuntimeException("保存本地消息失败");
            }
            
            // 直接发送消息（快速通路），发送结果再异步通过本地消息表做补偿
            try {
                messageProducer.sendMessage(topic, tag, messageDTO);
                localMessageService.updateStatus(messageDTO.getMessageId(), 
                        LocalMessage.MessageStatus.SENT.getCode());
                log.info("事件发布成功，Topic: {}, Tag: {}, EventId: {}, EventType: {}", 
                        topic, tag, event.getEventId(), event.getEventType());
            } catch (Exception sendEx) {
                log.error("事件即时发送失败，将通过本地消息表重试，Topic: {}, Tag: {}, EventId: {}", 
                        topic, tag, event.getEventId(), sendEx);
                localMessageService.updateStatus(messageDTO.getMessageId(), 
                        LocalMessage.MessageStatus.FAILED.getCode());
                localMessageService.incrementRetryCount(messageDTO.getMessageId());
            }
            
        } catch (Exception e) {
            log.error("事件发布失败，Topic: {}, Tag: {}, EventType: {}", 
                    topic, tag, event.getEventType(), e);
            throw new RuntimeException("事件发布失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发布订单相关事件
     * 
     * @param tag Tag名称
     * @param event 事件对象
     */
    public void publishOrderEvent(String tag, BaseEvent event) {
        publish(RocketMQTopicConfig.TOPIC_ORDER, tag, event);
    }
    
    /**
     * 发布库存相关事件
     * 
     * @param tag Tag名称
     * @param event 事件对象
     */
    public void publishInventoryEvent(String tag, BaseEvent event) {
        publish(RocketMQTopicConfig.TOPIC_INVENTORY, tag, event);
    }
    
    /**
     * 发布优惠券相关事件
     * 
     * @param tag Tag名称
     * @param event 事件对象
     */
    public void publishCouponEvent(String tag, BaseEvent event) {
        publish(RocketMQTopicConfig.TOPIC_COUPON, tag, event);
    }
    
    /**
     * 发布支付相关事件
     * 
     * @param tag Tag名称
     * @param event 事件对象
     */
    public void publishPaymentEvent(String tag, BaseEvent event) {
        publish(RocketMQTopicConfig.TOPIC_PAYMENT, tag, event);
    }
}
