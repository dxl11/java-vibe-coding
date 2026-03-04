package com.vibe.common.core.log.aspect;

import com.vibe.common.core.log.PerformanceLogger;
import com.vibe.common.core.log.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 消息队列日志切面
 * 记录消息发送和消费的日志
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Aspect
@Component
public class MqLoggingAspect {
    
    /**
     * 切点：RocketMQ Producer 方法
     */
    @Pointcut("execution(* com.vibe.common.mq.producer.impl.RocketMQProducer.*(..))")
    public void rocketMqProducerPointcut() {
    }
    
    /**
     * 切点：消息消费者方法
     */
    @Pointcut("execution(* com.vibe.common.mq.consumer.AbstractMessageConsumer.consume(..))")
    public void messageConsumerPointcut() {
    }
    
    /**
     * 环绕通知：记录消息发送日志
     * 
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("rocketMqProducerPointcut()")
    public Object logMessageSend(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        long startTime = System.currentTimeMillis();
        String traceId = TraceIdUtils.getTraceId();
        
        try {
            // 提取 Topic 和 Tag
            String topic = args.length > 0 ? String.valueOf(args[0]) : "unknown";
            String tag = args.length > 1 ? String.valueOf(args[1]) : "unknown";
            
            log.info("MQ_SEND|METHOD:{}|TOPIC:{}|TAG:{}|TRACE_ID:{}", 
                    methodName, topic, tag, traceId);
            
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            PerformanceLogger.logMqPerformance(topic, tag, "SEND", duration, true);
            
            log.info("MQ_SEND_SUCCESS|METHOD:{}|TOPIC:{}|TAG:{}|DURATION:{}ms|TRACE_ID:{}", 
                    methodName, topic, tag, duration, traceId);
            
            return result;
            
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            String topic = args.length > 0 ? String.valueOf(args[0]) : "unknown";
            String tag = args.length > 1 ? String.valueOf(args[1]) : "unknown";
            
            PerformanceLogger.logMqPerformance(topic, tag, "SEND", duration, false);
            
            log.error("MQ_SEND_FAILED|METHOD:{}|TOPIC:{}|TAG:{}|DURATION:{}ms|ERROR:{}|TRACE_ID:{}", 
                    methodName, topic, tag, duration, e.getMessage(), traceId, e);
            
            throw e;
        }
    }
    
    /**
     * 环绕通知：记录消息消费日志
     * 
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("messageConsumerPointcut()")
    public Object logMessageConsume(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        long startTime = System.currentTimeMillis();
        
        // 确保有 TraceId
        String traceId = TraceIdUtils.getTraceId();
        
        try {
            if (args.length > 0) {
                Object messageDTO = args[0];
                log.info("MQ_CONSUME|MESSAGE:{}|TRACE_ID:{}", messageDTO, traceId);
            }
            
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            PerformanceLogger.logMqPerformance("unknown", "unknown", "RECEIVE", duration, true);
            
            log.info("MQ_CONSUME_SUCCESS|DURATION:{}ms|TRACE_ID:{}", duration, traceId);
            
            return result;
            
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            PerformanceLogger.logMqPerformance("unknown", "unknown", "RECEIVE", duration, false);
            
            log.error("MQ_CONSUME_FAILED|DURATION:{}ms|ERROR:{}|TRACE_ID:{}", 
                    duration, e.getMessage(), traceId, e);
            
            throw e;
        }
    }
}
