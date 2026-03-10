package com.vibe.common.core.log.config;

import com.vibe.common.core.log.task.TraceIdTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 定时任务配置类
 * 配置 TraceId 装饰器，确保定时任务日志可追踪
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Configuration
@EnableScheduling
@EnableAsync
public class TaskConfig {
    
    /**
     * TraceId 任务装饰器
     * 
     * @return TraceIdTaskDecorator
     */
    @Bean
    public TraceIdTaskDecorator traceIdTaskDecorator() {
        return new TraceIdTaskDecorator();
    }
    
    /**
     * 异步任务执行器（带 TraceId 装饰）
     * 
     * @return Executor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.setTaskDecorator(traceIdTaskDecorator());
        executor.initialize();
        return executor;
    }
}
