package com.geektime.ratelimiter.rule;

/**
 * @Description: 限流规则管理接口
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public interface RateLimitRule {
    /**
     * 获取特定应用和API的限流配置
     * @param appId 应用ID
     * @param api API路径
     * @return 限流配置，如果不存在返回null
     */
    ApiLimit getLimit(String appId, String api);

    /**
     * 添加限流规则
     * @param limit 限流配置
     */
    void addLimit(ApiLimit limit);

    /**
     * 移除限流规则
     * @param appId 应用ID
     * @param api API路径
     */
    void removeLimit(String appId, String api);
}
