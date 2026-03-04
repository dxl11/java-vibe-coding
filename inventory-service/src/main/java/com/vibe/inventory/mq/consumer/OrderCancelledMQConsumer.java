package com.vibe.inventory.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.common.mq.config.RocketMQTopicConfig;
import com.vibe.common.mq.consumer.AbstractMessageConsumer;
import com.vibe.common.mq.dto.MessageDTO;
import com.vibe.inventory.listener.OrderCancelledListener;
import com.vibe.order.event.OrderCancelledEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 订单取消消息消费者
 * 将消息反序列化为领域事件并委托给领域事件监听器处理
 *
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQTopicConfig.TOPIC_ORDER,
        consumerGroup = RocketMQTopicConfig.CONSUMER_GROUP_INVENTORY,
        selectorExpression = "order-cancel",
        consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
        maxReconsumeTimes = 5
)
public class OrderCancelledMQConsumer extends AbstractMessageConsumer implements RocketMQListener<MessageDTO> {

    @Autowired
    private OrderCancelledListener orderCancelledListener;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void onMessage(MessageDTO message) {
        consume(message);
    }

    @Override
    protected boolean beforeConsume(MessageDTO messageDTO) {
        String businessKey = messageDTO.getKey();
        if (businessKey == null || businessKey.isEmpty()) {
            return true;
        }
        String redisKey = "mq:consume:idempotent:order_cancelled:" + businessKey;
        return com.vibe.common.core.idempotent.IdempotentUtils.checkAndSet(redisTemplate, redisKey, "1", 86400);
    }

    @Override
    protected void doConsume(MessageDTO messageDTO) {
        try {
            OrderCancelledEvent event = objectMapper.readValue(
                    messageDTO.getContent(), OrderCancelledEvent.class);
            event.setEventId(messageDTO.getMessageId());
            event.setEventType(messageDTO.getMessageType());
            event.setBusinessId(messageDTO.getKey());
            orderCancelledListener.onEvent(event);
        } catch (Exception e) {
            log.error("解析或处理订单取消消息失败，MessageId: {}", messageDTO.getMessageId(), e);
            throw new RuntimeException("订单取消消息处理失败", e);
        }
    }
}

