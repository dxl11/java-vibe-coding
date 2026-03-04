package com.vibe.common.core.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类
 * 提供缓存穿透、击穿、雪崩的防护
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public final class CacheUtils {
    
    /**
     * 空值缓存过期时间（秒），用于防止缓存穿透
     */
    private static final long NULL_VALUE_EXPIRE_SECONDS = 60;
    
    /**
     * 私有构造函数，防止实例化
     */
    private CacheUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 获取缓存（防止缓存穿透）
     * 如果缓存不存在，返回null，并设置空值缓存
     * 
     * @param redisTemplate Redis模板
     * @param key 缓存key
     * @return 缓存值，如果不存在返回null
     */
    public static String get(RedisTemplate<String, String> redisTemplate, String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            
            // 如果返回的是空值标记，说明之前查询过但不存在，直接返回null
            if (value != null && value.equals("__NULL__")) {
                return null;
            }
            
            return value;
            
        } catch (Exception e) {
            log.error("获取缓存失败，Key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 设置缓存
     * 
     * @param redisTemplate Redis模板
     * @param key 缓存key
     * @param value 缓存值
     * @param expireSeconds 过期时间（秒）
     */
    public static void set(RedisTemplate<String, String> redisTemplate, 
                           String key, String value, long expireSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置缓存失败，Key: {}", key, e);
        }
    }
    
    /**
     * 设置空值缓存（防止缓存穿透）
     * 
     * @param redisTemplate Redis模板
     * @param key 缓存key
     */
    public static void setNullValue(RedisTemplate<String, String> redisTemplate, String key) {
        set(redisTemplate, key, "__NULL__", NULL_VALUE_EXPIRE_SECONDS);
    }
    
    /**
     * 删除缓存
     * 
     * @param redisTemplate Redis模板
     * @param key 缓存key
     */
    public static void delete(RedisTemplate<String, String> redisTemplate, String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败，Key: {}", key, e);
        }
    }
    
    /**
     * 删除缓存（支持通配符）
     * 
     * @param redisTemplate Redis模板
     * @param pattern 缓存key模式
     */
    public static void deleteByPattern(RedisTemplate<String, String> redisTemplate, String pattern) {
        try {
            redisTemplate.delete(redisTemplate.keys(pattern));
        } catch (Exception e) {
            log.error("按模式删除缓存失败，Pattern: {}", pattern, e);
        }
    }
}
