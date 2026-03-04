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
 * SAGA 事务实体类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("saga_transaction")
public class SagaTransaction {
    
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 事务ID（全局唯一）
     */
    private String transactionId;
    
    /**
     * 业务ID（如订单号）
     */
    private String businessId;
    
    /**
     * 业务类型（如ORDER_CREATE）
     */
    private String businessType;
    
    /**
     * 事务状态
     */
    private Integer status;
    
    /**
     * 当前步骤
     */
    private String currentStep;
    
    /**
     * 已完成的步骤列表（JSON格式）
     */
    private String completedSteps;
    
    /**
     * 失败的步骤列表（JSON格式）
     */
    private String failedSteps;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
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
     * SAGA 事务状态枚举
     */
    public enum TransactionStatus {
        /**
         * 初始化
         */
        INIT(0, "初始化"),
        
        /**
         * 处理中
         */
        PROCESSING(1, "处理中"),
        
        /**
         * 已完成
         */
        COMPLETED(2, "已完成"),
        
        /**
         * 补偿中
         */
        COMPENSATING(3, "补偿中"),
        
        /**
         * 已补偿
         */
        COMPENSATED(4, "已补偿"),
        
        /**
         * 失败
         */
        FAILED(5, "失败");
        
        private final Integer code;
        private final String description;
        
        TransactionStatus(Integer code, String description) {
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
         * @return 事务状态枚举
         */
        public static TransactionStatus getByCode(Integer code) {
            if (code == null) {
                return null;
            }
            for (TransactionStatus status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }
}
