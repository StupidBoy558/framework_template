package com.geektime.ratelimiter.alg;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 滑动时间窗口限流算法
 */
public class SlidingWindowRateLimiter {
    private final int limit;
    private final long windowSizeInMs;
    private final Deque<Long> requestTimestamps;
    private final Lock lock = new ReentrantLock();

    /**
     * @param limit          时间窗口内最大请求数
     * @param windowSizeInMs 时间窗口大小（毫秒）
     */
    public SlidingWindowRateLimiter(int limit, long windowSizeInMs) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        if (windowSizeInMs <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        this.limit = limit;
        this.windowSizeInMs = windowSizeInMs;
        this.requestTimestamps = new ConcurrentLinkedDeque<>();
    }

    /**
     * 尝试获取许可
     *
     * @return 是否允许请求
     */
    public boolean tryAcquire() {
        long currentTime = System.currentTimeMillis();
        lock.lock();
        try {
            // 移除窗口外的请求
            while (!requestTimestamps.isEmpty() && (currentTime - requestTimestamps.peekFirst()) > windowSizeInMs) {
                requestTimestamps.pollFirst();
            }

            if (requestTimestamps.size() < limit) {
                requestTimestamps.addLast(currentTime);
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前时间窗口内的请求数
     *
     * @return 当前请求数
     */
    public int getCurrentCount() {
        long currentTime = System.currentTimeMillis();
        lock.lock();
        try {
            while (!requestTimestamps.isEmpty() && (currentTime - requestTimestamps.peekFirst()) > windowSizeInMs) {
                requestTimestamps.pollFirst();
            }
            return requestTimestamps.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取距离下一个请求进入窗口的剩余毫秒数
     *
     * @return 剩余毫秒数
     */
    public long getTimeToNextWindow() {
        lock.lock();
        try {
            if (requestTimestamps.isEmpty()) {
                return 0;
            }
            long currentTime = System.currentTimeMillis();
            long earliest = requestTimestamps.peekFirst();
            long elapsed = currentTime - earliest;
            if (elapsed >= windowSizeInMs) {
                return 0;
            } else {
                return windowSizeInMs - elapsed;
            }
        } finally {
            lock.unlock();
        }
    }
} 