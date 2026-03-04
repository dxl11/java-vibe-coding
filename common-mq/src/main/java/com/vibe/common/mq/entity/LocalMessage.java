package com.vibe.common.mq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 本地消息实体类
 * 用于可靠消息投递，实现本地消息表模式
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("local_message")
public class LocalMessage {
    
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 消息唯一标识
     */
    private String messageId;
    
    /**
     * Topic名称
     */
    private String topic;
    
    /**
     * Tag名称
     */
    private String tag;
    
    /**
     * 消息体（JSON格式）
     */
    private String messageBody;
    
    /**
     * 消息类型
     */
    private String messageType;
    
    /**
     * 状态：0-待发送，1-已发送，2-发送失败，3-已确认
     */
    private Integer status;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
    
    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;
    
    /**
     * 业务ID（如订单号）
     */
    private String businessId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    private Integer isDeleted;
    
    /**
     * 消息状态枚举
     */
    public enum MessageStatus {
        /**
         * 待发送
         */
        PENDING(0, "待发送"),
        
        /**
         * 已发送
         */
        SENT(1, "已发送"),
        
        /**
         * 发送失败
         */
        FAILED(2, "发送失败"),
        
        /**
         * 已确认
         */
        CONFIRMED(3, "已确认");
        
        private final Integer code;
        private final String description;
        
        MessageStatus(Integer code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public Integer getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * 根据状态码获取枚举
         * 
         * @param code 状态码
         * @return 消息状态枚举
         */
        public static MessageStatus getByCode(Integer code) {
            if (code == null) {
                return null;
            }
            for (MessageStatus status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }
}
