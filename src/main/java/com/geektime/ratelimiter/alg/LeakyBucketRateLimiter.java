package com.geektime.ratelimiter.alg;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 漏桶限流算法
 */
public class LeakyBucketRateLimiter {
    private final int capacity;
    private final long leakIntervalInMs;
    private final Queue<Long> bucket;
    private long lastLeakTime;

    /**
     * @param capacity         漏桶容量
     * @param leakIntervalInMs 漏出间隔（毫秒）
     */
    public LeakyBucketRateLimiter(int capacity, long leakIntervalInMs) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        if (leakIntervalInMs <= 0) {
            throw new IllegalArgumentException("Leak interval must be positive");
        }
        this.capacity = capacity;
        this.leakIntervalInMs = leakIntervalInMs;
        this.bucket = new LinkedList<>();
        this.lastLeakTime = System.currentTimeMillis();
    }

    /**
     * 尝试添加一个请求到漏桶
     *
     * @return 是否允许请求
     */
    public synchronized boolean tryAcquire() {
        leak();

        if (bucket.size() < capacity) {
            bucket.add(System.currentTimeMillis());
            return true;
        } else {
            return false;
        }
    }

    /**
     * 以固定速率漏出请求
     */
    private void leak() {
        long currentTime = System.currentTimeMillis();
        while (!bucket.isEmpty() && (currentTime - lastLeakTime) >= leakIntervalInMs) {
            bucket.poll();
            lastLeakTime += leakIntervalInMs;
        }

        // 防止时间回拨导致的异常
        if (currentTime - lastLeakTime >= leakIntervalInMs * 10) { // 10次漏出间隔
            lastLeakTime = currentTime;
        }
    }

    /**
     * 获取当前桶中的请求数
     *
     * @return 当前请求数
     */
    public synchronized int getCurrentCount() {
        leak();
        return bucket.size();
    }

    /**
     * 获取距离下次漏出的剩余毫秒数
     *
     * @return 剩余毫秒数
     */
    public synchronized long getTimeToNextLeak() {
        if (bucket.isEmpty()) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        long timeSinceLastLeak = currentTime - lastLeakTime;
        if (timeSinceLastLeak >= leakIntervalInMs) {
            return 0;
        } else {
            return leakIntervalInMs - timeSinceLastLeak;
        }
    }
} 