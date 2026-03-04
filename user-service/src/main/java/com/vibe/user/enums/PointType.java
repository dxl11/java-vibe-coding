package com.vibe.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分类型枚举
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Getter
@AllArgsConstructor
public enum PointType {
    
    /**
     * 获得
     */
    EARN(1, "获得"),
    
    /**
     * 消费
     */
    CONSUME(2, "消费");
    
    /**
     * 类型码
     */
    private final Integer code;
    
    /**
     * 类型描述
     */
    private final String description;
    
    /**
     * 根据类型码获取枚举
     * 
     * @param code 类型码
     * @return 积分类型枚举
     */
    public static PointType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PointType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
