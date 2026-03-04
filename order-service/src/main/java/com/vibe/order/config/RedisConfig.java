package com.vibe.order.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 配置Redis连接池和序列化方式
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Configuration
@EnableCaching
public class RedisConfig {
    
    /**
     * 配置RedisTemplate
     * 
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用String序列化器（用于key和value都是String的场景）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 配置Lettuce连接池
     * 使用Lettuce作为Redis客户端，支持异步和连接池
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        
        // 设置连接信息
        factory.setHostName(redisProperties.getHost());
        factory.setPort(redisProperties.getPort());
        if (redisProperties.getPassword() != null) {
            factory.setPassword(redisProperties.getPassword());
        }
        factory.setDatabase(redisProperties.getDatabase());
        
        // 配置连接池
        if (redisProperties.getLettuce() != null && redisProperties.getLettuce().getPool() != null) {
            RedisProperties.Pool pool = redisProperties.getLettuce().getPool();
            // Lettuce连接池配置通过application.yml中的spring.redis.lettuce.pool配置
        }
        
        return factory;
    }
}
