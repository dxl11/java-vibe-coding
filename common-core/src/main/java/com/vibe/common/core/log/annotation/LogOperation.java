package com.vibe.common.core.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 用于标记需要记录操作审计的方法
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogOperation {
    
    /**
     * 操作类型
     * 
     * @return 操作类型
     */
    String operation() default "";
    
    /**
     * 资源类型
     * 
     * @return 资源类型
     */
    String resource() default "";
    
    /**
     * 操作动作
     * CREATE/UPDATE/DELETE/QUERY等
     * 
     * @return 操作动作
     */
    String action() default "";
    
    /**
     * 是否记录请求参数
     * 
     * @return 是否记录
     */
    boolean recordParams() default true;
    
    /**
     * 是否记录响应结果
     * 
     * @return 是否记录
     */
    boolean recordResult() default true;
}
