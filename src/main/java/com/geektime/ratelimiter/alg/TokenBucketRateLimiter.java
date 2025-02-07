package com.geektime.ratelimiter.alg;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌桶限流算法
 */
public class TokenBucketRateLimiter {
    private final long capacity;
    private final long refillTokens;
    private final long refillIntervalInMs;
    private final AtomicLong availableTokens;
    private long lastRefillTimestamp;

    /**
     * @param capacity           令牌桶容量
     * @param refillTokens       每次填充的令牌数
     * @param refillIntervalInMs 填充间隔（毫秒）
     */
    public TokenBucketRateLimiter(long capacity, long refillTokens, long refillIntervalInMs) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        if (refillTokens <= 0) {
            throw new IllegalArgumentException("Refill tokens must be positive");
        }
        if (refillIntervalInMs <= 0) {
            throw new IllegalArgumentException("Refill interval must be positive");
        }
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillIntervalInMs = refillIntervalInMs;
        this.availableTokens = new AtomicLong(capacity);
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    /**
     * 尝试获取一个令牌
     *
     * @return 是否允许请求
     */
    public synchronized boolean tryAcquire() {
        refill();

        if (availableTokens.get() > 0) {
            availableTokens.decrementAndGet();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 重新填充令牌
     */
    private void refill() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRefillTimestamp;

        if (elapsedTime > refillIntervalInMs) {
            long refillCount = elapsedTime / refillIntervalInMs;
            long tokensToAdd = refillCount * refillTokens;
            long newTokenCount = Math.min(availableTokens.get() + tokensToAdd, capacity);
            availableTokens.set(newTokenCount);
            lastRefillTimestamp += refillCount * refillIntervalInMs;
        }
    }

    /**
     * 获取当前可用的令牌数
     *
     * @return 可用令牌数
     */
    public long getAvailableTokens() {
        refill();
        return availableTokens.get();
    }

    /**
     * 获取距离下次令牌填充的剩余毫秒数
     *
     * @return 剩余毫秒数
     */
    public long getTimeToNextRefill() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRefillTimestamp;
        long timeToNext = refillIntervalInMs - (elapsedTime % refillIntervalInMs);
        return timeToNext;
    }
} 