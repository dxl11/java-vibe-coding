package com.vibe.common.core.saga.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SAGA 步骤实体类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("saga_step")
public class SagaStep {
    
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 步骤ID（全局唯一）
     */
    private String stepId;
    
    /**
     * 事务ID
     */
    private String transactionId;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 步骤名称
     */
    private String stepName;
    
    /**
     * 步骤顺序
     */
    private Integer stepOrder;
    
    /**
     * 步骤状态
     */
    private Integer status;
    
    /**
     * 请求数据（JSON格式）
     */
    private String requestData;
    
    /**
     * 响应数据（JSON格式）
     */
    private String responseData;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行时间
     */
    private LocalDateTime executeTime;
    
    /**
     * 补偿时间
     */
    private LocalDateTime compensateTime;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;
    
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
     * SAGA 步骤状态枚举
     */
    public enum StepStatus {
        /**
         * 待执行
         */
        PENDING(0, "待执行"),
        
        /**
         * 成功
         */
        SUCCESS(1, "成功"),
        
        /**
         * 失败
         */
        FAILED(2, "失败"),
        
        /**
         * 已补偿
         */
        COMPENSATED(3, "已补偿");
        
        private final Integer code;
        private final String description;
        
        StepStatus(Integer code, String description) {
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
         * @return 步骤状态枚举
         */
        public static StepStatus getByCode(Integer code) {
            if (code == null) {
                return null;
            }
            for (StepStatus status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }
}
