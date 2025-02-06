package com.geektime.ratelimiter.alg;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: 固定时间窗口限流算法实现
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:06
 **/
public class FixedTimeWinRateLimitAlg implements RateLimitAlg {
    private static final long TRY_LOCK_TIMEOUT = 200L;  // 200ms.
    private final Stopwatch stopwatch;
    private final AtomicInteger currentCount = new AtomicInteger(0);
    private final AtomicLong lastResetTime;
    private final int limit;
    private final long windowSizeInMs;
    private final Lock lock = new ReentrantLock();

    /**
     * @param limit 时间窗口内最大请求数
     * @param windowSizeInMs 时间窗口大小（毫秒）
     */
    public FixedTimeWinRateLimitAlg(int limit, long windowSizeInMs) {
        this(limit, windowSizeInMs, Stopwatch.createStarted());
    }

    @VisibleForTesting
    protected FixedTimeWinRateLimitAlg(int limit, long windowSizeInMs, Stopwatch stopwatch) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        if (windowSizeInMs <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }

        this.limit = limit;
        this.windowSizeInMs = windowSizeInMs;
        this.stopwatch = stopwatch;
        this.lastResetTime = new AtomicLong(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean tryAcquire() throws InternalErrorException {
        long currentTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        long lastReset = lastResetTime.get();

        // 检查是否需要重置计数器
        if (currentTime - lastReset > windowSizeInMs) {
            try {
                if (lock.tryLock(TRY_LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    try {
                        // 双重检查，防止并发重置
                        lastReset = lastResetTime.get();
                        if (currentTime - lastReset > windowSizeInMs) {
                            // 处理时钟回拨
                            if (currentTime < lastReset) {
                                throw new InternalErrorException("Clock moved backwards");
                            }
                            currentCount.set(0);
                            lastResetTime.set(currentTime);
                        }
                    } finally {
                        lock.unlock();
                    }
                } else {
                    throw new InternalErrorException("tryAcquire() wait lock too long:" + TRY_LOCK_TIMEOUT + "ms");
                }
            } catch (InterruptedException e) {
                throw new InternalErrorException("tryAcquire() is interrupted by lock-time-out.", e);
            }
        }

        // 尝试增加计数
        int updatedCount = currentCount.incrementAndGet();
        if (updatedCount <= limit) {
            return true;
        }

        // 超过限制，将计数减回去
        currentCount.decrementAndGet();
        return false;
    }

    @Override
    public int getCurrentCount() {
        return currentCount.get();
    }

    @Override
    public long getTimeToNextWindow() {
        long currentTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        long lastReset = lastResetTime.get();
        if (currentTime < lastReset) {
            return 0; // 时钟回拨情况下，立即重置
        }
        return Math.max(0, windowSizeInMs - (currentTime - lastReset));
    }
} 