package com.vibe.common.mq.producer.impl;

import com.vibe.common.mq.dto.MessageDTO;
import com.vibe.common.mq.producer.MessageProducer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * RocketMQ 消息生产者实现
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class RocketMQProducer implements MessageProducer {
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    private Timer sendTimer;
    private Counter sendSuccessCounter;
    private Counter sendFailureCounter;

    @PostConstruct
    public void init() {
        if (meterRegistry != null) {
            sendTimer = Timer.builder("mq.producer.send.duration")
                    .description("消息发送耗时")
                    .register(meterRegistry);

            sendSuccessCounter = Counter.builder("mq.producer.send.count")
                    .tag("status", "success")
                    .description("消息发送成功次数")
                    .register(meterRegistry);

            sendFailureCounter = Counter.builder("mq.producer.send.count")
                    .tag("status", "failed")
                    .description("消息发送失败次数")
                    .register(meterRegistry);
        }
    }
    
    @Override
    public String sendMessage(String topic, MessageDTO messageDTO) {
        return sendSyncMessage(topic, messageDTO);
    }

    @Override
    public String sendMessage(String topic, String tag, MessageDTO messageDTO) {
        return sendSyncMessage(topic, tag, messageDTO);
    }
    
    @Override
    public String sendSyncMessage(String topic, MessageDTO messageDTO) {
        return sendSyncMessage(topic, null, messageDTO);
    }
    
    @Override
    public String sendSyncMessage(String topic, String tag, MessageDTO messageDTO) {
        try {
            // 生成消息ID
            String messageId = generateMessageId(messageDTO);
            
            // 构建 RocketMQ 消息
            String destination = tag != null && !tag.isEmpty() 
                    ? topic + ":" + tag 
                    : topic;
            
            org.springframework.messaging.Message<MessageDTO> rocketMessage = MessageBuilder
                    .withPayload(messageDTO)
                    .setHeader("KEYS", messageId)
                    .build();
            
            long start = System.currentTimeMillis();
            // 同步发送消息
            SendResult sendResult = rocketMQTemplate.syncSend(destination, rocketMessage);
            long duration = System.currentTimeMillis() - start;
            recordSendMetrics(true, duration);

            log.info("RocketMQ 同步消息发送成功，Topic: {}, Tag: {}, MessageId: {}, SendResult: {}",
                    topic, tag, messageId, sendResult.getMsgId());
            
            return messageId;
            
        } catch (Exception e) {
            recordSendMetrics(false, 0);
            log.error("RocketMQ 同步消息发送失败，Topic: {}, Tag: {}", topic, tag, e);
            throw new RuntimeException("消息发送失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String sendAsyncMessage(String topic, MessageDTO messageDTO) {
        return sendAsyncMessage(topic, null, messageDTO);
    }
    
    @Override
    public String sendAsyncMessage(String topic, String tag, MessageDTO messageDTO) {
        try {
            // 生成消息ID
            String messageId = generateMessageId(messageDTO);
            
            // 构建 RocketMQ 消息
            String destination = tag != null && !tag.isEmpty() 
                    ? topic + ":" + tag 
                    : topic;
            
            org.springframework.messaging.Message<MessageDTO> rocketMessage = MessageBuilder
                    .withPayload(messageDTO)
                    .setHeader("KEYS", messageId)
                    .build();
            
            long start = System.currentTimeMillis();
            // 异步发送消息
            rocketMQTemplate.asyncSend(
                    destination,
                    rocketMessage,
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            recordSendMetrics(true, System.currentTimeMillis() - start);
                            log.info("RocketMQ 异步消息发送成功，Topic: {}, Tag: {}, MessageId: {}, SendResult: {}",
                                    topic, tag, messageId, sendResult.getMsgId());
                        }
                        
                        @Override
                        public void onException(Throwable e) {
                            recordSendMetrics(false, System.currentTimeMillis() - start);
                            log.error("RocketMQ 异步消息发送失败，Topic: {}, Tag: {}, MessageId: {}",
                                    topic, tag, messageId, e);
                        }
                    }
            );
            
            log.info("RocketMQ 异步消息已提交，Topic: {}, Tag: {}, 消息ID: {}", topic, tag, messageId);
            return messageId;
            
        } catch (Exception e) {
            recordSendMetrics(false, 0);
            log.error("RocketMQ 异步消息发送异常，Topic: {}, Tag: {}", topic, tag, e);
            throw new RuntimeException("消息发送失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String sendOrderlyMessage(String topic, String tag, MessageDTO messageDTO, String orderKey) {
        try {
            // 生成消息ID
            String messageId = generateMessageId(messageDTO);
            
            // 构建 RocketMQ 消息
            String destination = tag != null && !tag.isEmpty() 
                    ? topic + ":" + tag 
                    : topic;
            
            org.springframework.messaging.Message<MessageDTO> rocketMessage = MessageBuilder
                    .withPayload(messageDTO)
                    .setHeader("KEYS", messageId)
                    .build();
            
            long start = System.currentTimeMillis();
            // 发送顺序消息（使用 orderKey 路由到同一队列）
            SendResult sendResult = rocketMQTemplate.syncSendOrderly(
                    destination,
                    rocketMessage,
                    orderKey
            );
            long duration = System.currentTimeMillis() - start;
            recordSendMetrics(true, duration);
            
            log.info("RocketMQ 顺序消息发送成功，Topic: {}, Tag: {}, OrderKey: {}, MessageId: {}",
                    topic, tag, orderKey, messageId);
            
            return messageId;
            
        } catch (Exception e) {
            recordSendMetrics(false, 0);
            log.error("RocketMQ 顺序消息发送失败，Topic: {}, Tag: {}", topic, tag, e);
            throw new RuntimeException("消息发送失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String sendDelayMessage(String topic, String tag, MessageDTO messageDTO, int delayLevel) {
        try {
            // 生成消息ID
            String messageId = generateMessageId(messageDTO);
            
            // 构建 RocketMQ 消息
            String destination = tag != null && !tag.isEmpty() 
                    ? topic + ":" + tag 
                    : topic;
            
            org.springframework.messaging.Message<MessageDTO> rocketMessage = MessageBuilder
                    .withPayload(messageDTO)
                    .setHeader("KEYS", messageId)
                    .build();
            
            long start = System.currentTimeMillis();
            // 发送延时消息
            SendResult sendResult = rocketMQTemplate.syncSend(
                    destination,
                    rocketMessage,
                    3000,  // 超时时间（毫秒）
                    delayLevel  // 延时级别
            );
            long duration = System.currentTimeMillis() - start;
            recordSendMetrics(true, duration);
            
            log.info("RocketMQ 延时消息发送成功，Topic: {}, Tag: {}, DelayLevel: {}, MessageId: {}",
                    topic, tag, delayLevel, messageId);
            
            return messageId;
            
        } catch (Exception e) {
            recordSendMetrics(false, 0);
            log.error("RocketMQ 延时消息发送失败，Topic: {}, Tag: {}", topic, tag, e);
            throw new RuntimeException("消息发送失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成消息ID
     * 
     * @param messageDTO 消息DTO
     * @return 消息ID
     */
    private String generateMessageId(MessageDTO messageDTO) {
        if (messageDTO.getMessageId() != null && !messageDTO.getMessageId().isEmpty()) {
            return messageDTO.getMessageId();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 记录发送指标
     *
     * @param success  是否成功
     * @param durationMs 耗时（毫秒），异步异常等场景可传 0
     */
    private void recordSendMetrics(boolean success, long durationMs) {
        if (meterRegistry == null) {
            return;
        }
        if (success) {
            if (sendSuccessCounter != null) {
                sendSuccessCounter.increment();
            }
        } else {
            if (sendFailureCounter != null) {
                sendFailureCounter.increment();
            }
        }
        if (sendTimer != null && durationMs > 0) {
            sendTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }
}
