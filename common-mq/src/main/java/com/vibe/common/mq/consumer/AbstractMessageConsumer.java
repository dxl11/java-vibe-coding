package com.vibe.common.mq.consumer;

import com.vibe.common.mq.dto.MessageDTO;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * 消息消费者抽象类
 * 提供通用的消息处理逻辑
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public abstract class AbstractMessageConsumer implements MessageConsumer {

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    private Timer consumeTimer;
    private Counter consumeSuccessCounter;
    private Counter consumeFailureCounter;

    @PostConstruct
    public void initMetrics() {
        if (meterRegistry != null) {
            consumeTimer = Timer.builder("mq.consumer.consume.duration")
                    .description("消息消费耗时")
                    .register(meterRegistry);
            consumeSuccessCounter = Counter.builder("mq.consumer.consume.count")
                    .tag("status", "success")
                    .description("消息消费成功次数")
                    .register(meterRegistry);
            consumeFailureCounter = Counter.builder("mq.consumer.consume.count")
                    .tag("status", "failed")
                    .description("消息消费失败次数")
                    .register(meterRegistry);
        }
    }
    
    @Override
    public void consume(MessageDTO messageDTO) {
        long start = System.currentTimeMillis();
        try {
            log.info("开始消费消息，MessageId: {}, MessageType: {}, BusinessKey: {}", 
                    messageDTO.getMessageId(), messageDTO.getMessageType(), messageDTO.getKey());

            // 幂等性检查（可由子类关闭或自定义 key）
            if (!beforeConsume(messageDTO)) {
                log.warn("检测到重复消费，跳过处理，MessageId: {}, BusinessKey: {}",
                        messageDTO.getMessageId(), messageDTO.getKey());
                return;
            }
            
            // 执行业务处理
            doConsume(messageDTO);
            
            afterConsume(messageDTO);
            
            recordConsumeMetrics(true, System.currentTimeMillis() - start);
            log.info("消息消费成功，MessageId: {}", messageDTO.getMessageId());
            
        } catch (Exception e) {
            recordConsumeMetrics(false, System.currentTimeMillis() - start);
            log.error("消息消费失败，MessageId: {}", messageDTO.getMessageId(), e);
            handleException(messageDTO, e);
            // 将异常抛出给底层 MQ 框架，以便触发重试或进入死信队列
            throw e;
        }
    }
    
    /**
     * 消费前处理
     * 默认实现支持基于消息键的幂等性检查，子类可重写以自定义幂等 key 或关闭幂等
     *
     * @param messageDTO 消息DTO
     * @return true 表示可以继续消费；false 表示检测到重复消费应跳过
     */
    protected boolean beforeConsume(MessageDTO messageDTO) {
        // 默认不开启幂等（返回 true），具体业务可在子类中结合 IdempotentUtils 实现
        return true;
    }

    /**
     * 执行业务处理
     * 子类需要实现此方法
     * 
     * @param messageDTO 消息DTO
     */
    protected abstract void doConsume(MessageDTO messageDTO);
    
    /**
     * 消费后处理
     * 子类可以重写此方法实现消费日志记录等
     *
     * @param messageDTO 消息DTO
     */
    protected void afterConsume(MessageDTO messageDTO) {
        // 默认不做处理
    }

    /**
     * 处理异常
     * 子类可以重写此方法实现自定义异常处理
     * 
     * @param messageDTO 消息DTO
     * @param e 异常
     */
    protected void handleException(MessageDTO messageDTO, Exception e) {
        // 默认实现：记录日志，可以扩展为发送到死信队列等
        log.error("消息消费异常处理，MessageId: {}", messageDTO.getMessageId(), e);
    }

    private void recordConsumeMetrics(boolean success, long durationMs) {
        if (meterRegistry == null) {
            return;
        }
        if (success) {
            if (consumeSuccessCounter != null) {
                consumeSuccessCounter.increment();
            }
        } else {
            if (consumeFailureCounter != null) {
                consumeFailureCounter.increment();
            }
        }
        if (consumeTimer != null && durationMs > 0) {
            consumeTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }
}
