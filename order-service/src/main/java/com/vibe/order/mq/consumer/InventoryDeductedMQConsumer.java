package com.vibe.order.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.common.mq.config.RocketMQTopicConfig;
import com.vibe.common.mq.consumer.AbstractMessageConsumer;
import com.vibe.common.mq.dto.MessageDTO;
import com.vibe.inventory.event.InventoryDeductedEvent;
import com.vibe.order.listener.InventoryDeductedListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 库存扣减成功消息消费者
 * 将消息反序列化为领域事件并委托给领域事件监听器处理
 *
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQTopicConfig.TOPIC_INVENTORY,
        consumerGroup = RocketMQTopicConfig.CONSUMER_GROUP_ORDER,
        selectorExpression = "inventory-deducted",
        consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
        maxReconsumeTimes = 5
)
public class InventoryDeductedMQConsumer extends AbstractMessageConsumer implements RocketMQListener<MessageDTO> {

    @Autowired
    private InventoryDeductedListener inventoryDeductedListener;

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
        String redisKey = "mq:consume:idempotent:inventory_deducted:" + businessKey;
        // 默认 24 小时内同一业务键只处理一次
        return com.vibe.common.core.idempotent.IdempotentUtils.checkAndSet(redisTemplate, redisKey, "1", 86400);
    }

    @Override
    protected void doConsume(MessageDTO messageDTO) {
        try {
            InventoryDeductedEvent event = objectMapper.readValue(
                    messageDTO.getContent(), InventoryDeductedEvent.class);
            // 补充事件公共字段，便于日志与追踪
            event.setEventId(messageDTO.getMessageId());
            event.setEventType(messageDTO.getMessageType());
            event.setBusinessId(messageDTO.getKey());
            inventoryDeductedListener.onEvent(event);
        } catch (Exception e) {
            log.error("解析或处理库存扣减成功消息失败，MessageId: {}", messageDTO.getMessageId(), e);
            throw new RuntimeException("库存扣减成功消息处理失败", e);
        }
    }
}

