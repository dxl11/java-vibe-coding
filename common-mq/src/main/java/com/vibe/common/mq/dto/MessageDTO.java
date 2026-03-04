package com.vibe.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 消息传输对象（DTO）
 * 用于消息发送和接收
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID（可选，如果为空则系统自动生成）
     */
    private String messageId;
    
    /**
     * 消息类型
     * 必填字段，用于路由到不同的业务处理器
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     * 必填字段，通常为 JSON 格式的业务数据
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 消息来源
     */
    private String source;
    
    /**
     * 消息目标
     */
    private String target;
    
    /**
     * 延迟时间（秒）
     * 用于延时消息，0 表示立即发送
     */
    private Long delaySeconds;
    
    /**
     * 消息标签（用于 RocketMQ）
     * 用于消息过滤和分类
     */
    private String tag;
    
    /**
     * 消息键（用于 Kafka）
     * 用于分区路由
     */
    private String key;
    
    /**
     * 备注信息
     */
    private String remark;
}
