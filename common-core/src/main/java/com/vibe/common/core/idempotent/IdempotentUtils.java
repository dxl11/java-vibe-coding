package com.vibe.common.core.idempotent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性工具类
 * 使用 Redis 实现分布式锁和幂等性保证
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public final class IdempotentUtils {
    
    /**
     * 幂等性检查的 Lua 脚本
     * 如果 key 不存在，设置 key 和过期时间，返回 1
     * 如果 key 已存在，返回 0
     */
    private static final String IDEMPOTENT_SCRIPT = 
            "if redis.call('get', KEYS[1]) == false then " +
            "    redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2]) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";
    
    /**
     * 私有构造函数，防止实例化
     */
    private IdempotentUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 检查并设置幂等性标记
     * 
     * @param redisTemplate Redis模板
     * @param key 幂等性key（通常是业务ID）
     * @param value 值
     * @param expireSeconds 过期时间（秒）
     * @return true-首次请求（可以处理），false-重复请求（需要忽略）
     */
    public static boolean checkAndSet(RedisTemplate<String, String> redisTemplate, 
                                      String key, String value, long expireSeconds) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(IDEMPOTENT_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, 
                    Collections.singletonList(key), value, String.valueOf(expireSeconds));
            
            return result != null && result == 1;
            
        } catch (Exception e) {
            log.error("幂等性检查失败，Key: {}", key, e);
            // 异常时返回 true，允许处理（避免 Redis 故障导致业务阻塞）
            return true;
        }
    }
    
    /**
     * 检查幂等性（使用默认过期时间 24 小时）
     * 
     * @param redisTemplate Redis模板
     * @param key 幂等性key
     * @return true-首次请求，false-重复请求
     */
    public static boolean checkAndSet(RedisTemplate<String, String> redisTemplate, String key) {
        return checkAndSet(redisTemplate, key, "1", 86400);
    }
    
    /**
     * 释放幂等性标记
     * 
     * @param redisTemplate Redis模板
     * @param key 幂等性key
     */
    public static void release(RedisTemplate<String, String> redisTemplate, String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("释放幂等性标记失败，Key: {}", key, e);
        }
    }
    
    /**
     * 获取分布式锁（简单版本，不支持续期）
     * 
     * @param redisTemplate Redis模板
     * @param lockKey 锁的key
     * @param lockValue 锁的值（用于释放时验证）
     * @param expireSeconds 过期时间（秒）
     * @return true-获取成功，false-获取失败
     */
    public static boolean tryLock(RedisTemplate<String, String> redisTemplate, 
                                 String lockKey, String lockValue, long expireSeconds) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 
                    expireSeconds, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Exception e) {
            log.error("获取分布式锁失败，LockKey: {}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 获取分布式锁（带自动续期）
     * 
     * @param redisTemplate Redis模板
     * @param lockKey 锁的key
     * @param expireSeconds 过期时间（秒）
     * @return 分布式锁对象，如果获取失败返回null
     */
    public static DistributedLock tryLockWithRenewal(RedisTemplate<String, String> redisTemplate, 
                                                     String lockKey, long expireSeconds) {
        DistributedLock lock = new DistributedLock(redisTemplate, lockKey, expireSeconds);
        if (lock.tryLock()) {
            return lock;
        }
        return null;
    }
    
    /**
     * 释放分布式锁
     * 
     * @param redisTemplate Redis模板
     * @param lockKey 锁的key
     * @param lockValue 锁的值（用于验证）
     */
    public static void releaseLock(RedisTemplate<String, String> redisTemplate, 
                                  String lockKey, String lockValue) {
        try {
            String script = 
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";
            
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            
            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
            
        } catch (Exception e) {
            log.error("释放分布式锁失败，LockKey: {}", lockKey, e);
        }
    }
}
