package com.geektime.ratelimiter.alg;

import com.geektime.ratelimiter.exception.InternalErrorException;

/**
 * @Description: 限流算法接口
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:06
 **/
public interface RateLimitAlg {
    /**
     * 尝试获取一个令牌
     * @return 是否获取成功
     * @throws InternalErrorException 当发生内部错误时抛出
     */
    boolean tryAcquire() throws InternalErrorException;

    /**
     * 获取当前时间窗口内的请求数
     */
    int getCurrentCount();

    /**
     * 获取距离下一个时间窗口的剩余毫秒数
     */
    long getTimeToNextWindow();
}
