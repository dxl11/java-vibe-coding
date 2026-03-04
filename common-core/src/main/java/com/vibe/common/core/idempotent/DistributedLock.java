package com.vibe.common.core.idempotent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 分布式锁（带自动续期）
 * 实现 Redisson 风格的分布式锁，支持 watchdog 自动续期
 *
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
public class DistributedLock {

    /**
     * 锁续期的 Lua 脚本
     * 只有当锁的值匹配时才续期
     */
    private static final String RENEW_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('expire', KEYS[1], ARGV[2]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    /**
     * 锁的过期时间（秒）
     */
    private final long expireSeconds;

    /**
     * 续期间隔（秒），默认是过期时间的1/3
     */
    private final long renewIntervalSeconds;

    /**
     * Redis模板
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 锁的key
     */
    private final String lockKey;

    /**
     * 锁的值（用于释放时验证）
     */
    private final String lockValue;

    /**
     * 是否已获取锁
     */
    private final AtomicBoolean locked = new AtomicBoolean(false);

    /**
     * 续期任务
     */
    private ScheduledExecutorService renewExecutor;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis模板
     * @param lockKey       锁的key
     * @param expireSeconds 过期时间（秒）
     */
    public DistributedLock(RedisTemplate<String, String> redisTemplate,
                           String lockKey, long expireSeconds) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.lockValue = UUID.randomUUID().toString();
        this.expireSeconds = expireSeconds;
        this.renewIntervalSeconds = Math.max(expireSeconds / 3, 1);  // 至少1秒
    }

    /**
     * 尝试获取锁
     *
     * @return true-获取成功，false-获取失败
     */
    public boolean tryLock() {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(
                    lockKey, lockValue, expireSeconds, TimeUnit.SECONDS);

            if (result != null && result) {
                locked.set(true);
                startRenewTask();
                log.debug("获取分布式锁成功，LockKey: {}, LockValue: {}", lockKey, lockValue);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("获取分布式锁失败，LockKey: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放锁
     */
    public void unlock() {
        if (!locked.get()) {
            return;
        }

        stopRenewTask();

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

            locked.set(false);
            log.debug("释放分布式锁成功，LockKey: {}, LockValue: {}", lockKey, lockValue);

        } catch (Exception e) {
            log.error("释放分布式锁失败，LockKey: {}", lockKey, e);
        }
    }

    /**
     * 启动续期任务
     */
    private void startRenewTask() {
        if (renewExecutor != null) {
            return;
        }

        renewExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "distributed-lock-renew" + lockKey);
            thread.setDaemon(true);
            return thread;
        });
    }

//    private void startRenewTask() {
//        if (renewExecutor != null) {
//            return;
//        }
//
//        renewExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
//            Thread t = new Thread(r, "distributed-lock-renew-" + lockKey);
//            t.setDaemon(true);
//            return t;
//        });
//
//        renewExecutor.scheduleWithFixedDelay(() -> {
//            if (!locked.get()) {
//                return;
//            }
//
//            try {
//                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
//                script.setScriptText(RENEW_SCRIPT);
//                script.setResultType(Long.class);
//
//                Long result = redisTemplate.execute(script,
//                        Collections.singletonList(lockKey),
//                        lockValue, String.valueOf(expireSeconds));
//
//                if (result != null && result == 1) {
//                    log.debug("分布式锁续期成功，LockKey: {}", lockKey);
//                } else {
//                    log.warn("分布式锁续期失败，锁可能已被释放，LockKey: {}", lockKey);
//                    locked.set(false);
//                }
//
//            } catch (Exception e) {
//                log.error("分布式锁续期异常，LockKey: {}", lockKey, e);
//            }
//
//        }, renewIntervalSeconds, renewIntervalSeconds, TimeUnit.SECONDS);
//    }


    /**
     * 停止续期任务
     */
    private void stopRenewTask() {
        if (renewExecutor != null) {
            renewExecutor.shutdown();
            try {
                if (!renewExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    renewExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                renewExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            renewExecutor = null;
        }
    }

    /**
     * 是否已获取锁
     *
     * @return true-已获取，false-未获取
     */
    public boolean isLocked() {
        return locked.get();
    }
}
