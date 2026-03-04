package com.vibe.common.core.monitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 性能监控配置属性
 * 从配置文件中读取性能监控相关配置
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
@Component
@ConfigurationProperties(prefix = "vibe.monitor.performance")
public class PerformanceMonitorProperties {
    
    /**
     * 默认执行时间阈值（毫秒）
     * 当注解中未指定阈值时使用此值
     */
    private long defaultThreshold = 2000;
    
    /**
     * 是否启用性能监控
     * 默认启用
     */
    private boolean enabled = true;
    
    /**
     * 是否记录所有方法的执行时间（即使未超过阈值）
     * 默认 false，只记录超过阈值的方法
     */
    private boolean recordAll = false;
}
