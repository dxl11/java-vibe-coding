package com.vibe.common.mq.task;

import com.vibe.common.mq.entity.LocalMessage;
import com.vibe.common.mq.service.LocalMessageService;
import com.vibe.common.mq.dto.MessageDTO;
import com.vibe.common.mq.producer.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地消息发送定时任务
 * 定期扫描本地消息表并重试发送失败的消息
 *
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class LocalMessageSendTask {
    
    @Autowired
    private LocalMessageService localMessageService;
    
    @Autowired
    private MessageProducer messageProducer;
    
    /**
     * 每30秒扫描待发送的消息
     */
    @Scheduled(fixedRate = 30000)
    public void sendPendingMessages() {
        try {
            List<LocalMessage> pendingMessages = localMessageService.getPendingMessages(100);
            if (pendingMessages.isEmpty()) {
                return;
            }
            
            log.info("本地消息发送任务：发现待发送消息 {} 条", pendingMessages.size());
            
            for (LocalMessage message : pendingMessages) {
                sendMessage(message);
            }
        } catch (Exception e) {
            log.error("本地消息发送任务执行异常", e);
        }
    }
    
    /**
     * 每60秒扫描需要重试的消息
     * 使用 LocalMessage 中的 maxRetryCount 与 nextRetryTime 控制重试节奏
     */
    @Scheduled(fixedRate = 60000)
    public void retryFailedMessages() {
        try {
            List<LocalMessage> retryMessages = localMessageService.getRetryMessages(100);
            if (retryMessages.isEmpty()) {
                return;
            }
            
            log.info("本地消息重试任务：发现需要重试的消息 {} 条", retryMessages.size());
            
            for (LocalMessage message : retryMessages) {
                sendMessage(message);
            }
        } catch (Exception e) {
            log.error("本地消息重试任务执行异常", e);
        }
    }
    
    /**
     * 发送单条消息并更新状态
     *
     * @param localMessage 本地消息
     */
    private void sendMessage(LocalMessage localMessage) {
        try {
            MessageDTO messageDTO = MessageDTO.builder()
                    .messageId(localMessage.getMessageId())
                    .messageType(localMessage.getMessageType())
                    .title(localMessage.getMessageType())
                    .content(localMessage.getMessageBody())
                    .source("local-message-task")
                    .tag(localMessage.getTag())
                    .key(localMessage.getBusinessId())
                    .build();
            
            messageProducer.sendMessage(localMessage.getTopic(), localMessage.getTag(), messageDTO);
            localMessageService.updateStatus(localMessage.getMessageId(),
                    LocalMessage.MessageStatus.SENT.getCode());
            
            log.info("本地消息发送成功，MessageId: {}", localMessage.getMessageId());
        } catch (Exception e) {
            log.error("本地消息发送失败，MessageId: {}", localMessage.getMessageId(), e);
            // 增加重试次数
            localMessageService.incrementRetryCount(localMessage.getMessageId());

            Integer retryCount = localMessage.getRetryCount() == null ? 0 : localMessage.getRetryCount();
            Integer maxRetry = localMessage.getMaxRetryCount() == null ? 3 : localMessage.getMaxRetryCount();

            if (retryCount + 1 >= maxRetry) {
                // 超过最大重试次数，标记为发送失败（死信），后续可由人工或专用任务处理
                localMessageService.updateStatus(localMessage.getMessageId(),
                        LocalMessage.MessageStatus.FAILED.getCode());
                log.warn("本地消息达到最大重试次数，标记为失败待人工处理，MessageId: {}, RetryCount: {}, MaxRetry: {}",
                        localMessage.getMessageId(), retryCount + 1, maxRetry);
            } else {
                // 仍未达到最大重试次数，保持/回退为待发送状态，等待下次重试
                localMessageService.updateStatus(localMessage.getMessageId(),
                        LocalMessage.MessageStatus.PENDING.getCode());
            }
        }
    }
}

