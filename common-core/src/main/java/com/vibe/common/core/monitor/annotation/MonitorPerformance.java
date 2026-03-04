package com.vibe.common.core.monitor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 性能监控注解
 * 用于标记需要监控执行时间的方法
 * 当方法执行时间超过阈值时，会自动记录到性能日志
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorPerformance {
    
    /**
     * 执行时间阈值（毫秒）
     * 默认值 -1 表示使用配置文件中的默认阈值
     * 
     * @return 阈值（毫秒）
     */
    long threshold() default -1;
    
    /**
     * 操作名称
     * 用于日志记录，便于识别监控的操作
     * 
     * @return 操作名称
     */
    String operation() default "";
    
    /**
     * 是否异步记录
     * true: 异步记录，不影响业务性能
     * false: 同步记录
     * 
     * @return 是否异步
     */
    boolean async() default true;
}
