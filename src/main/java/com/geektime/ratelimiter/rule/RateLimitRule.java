package com.geektime.ratelimiter.rule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 限流规则管理类
 * 该类负责管理所有API的限流规则，提供规则的增删改查功能。
 * 使用ConcurrentHashMap保证线程安全，支持高并发访问。
 * 
 * 使用示例：
 * RuleConfig config = new RuleConfig();
 * List<ApiLimit> limits = new ArrayList<>();
 * limits.add(new ApiLimit("app1", "/api/v1/user", 100, 1));
 * config.setLimits(limits);
 * 
 * RateLimitRule rule = new RateLimitRule(config);
 * ApiLimit limit = rule.getLimit("app1", "/api/v1/user");
 * 
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public class RateLimitRule {
    // 使用ConcurrentHashMap存储限流规则，key为appId:api的组合
    private final Map<String, ApiLimit> limitMap = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，初始化限流规则
     * @param ruleConfig 规则配置
     */
    public RateLimitRule(RuleConfig ruleConfig) {
        // 从配置中加载限流规则
        if (ruleConfig != null && ruleConfig.getLimits() != null) {
            for (ApiLimit limit : ruleConfig.getLimits()) {
                String key = generateKey(limit.getAppId(), limit.getApi());
                limitMap.put(key, limit);
            }
        }
    }

    /**
     * 获取特定应用和API的限流配置
     * @param appId 应用ID
     * @param api API路径
     * @return 限流配置，如果不存在返回null
     */
    public ApiLimit getLimit(String appId, String api) {
        if (appId == null || api == null) {
            return null;
        }
        return limitMap.get(generateKey(appId, api));
    }

    /**
     * 添加限流规则
     * @param limit 限流配置
     */
    public void addLimit(ApiLimit limit) {
        if (limit != null) {
            String key = generateKey(limit.getAppId(), limit.getApi());
            limitMap.put(key, limit);
        }
    }

    /**
     * 移除限流规则
     * @param appId 应用ID
     * @param api API路径
     */
    public void removeLimit(String appId, String api) {
        if (appId != null && api != null) {
            String key = generateKey(appId, api);
            limitMap.remove(key);
        }
    }

    /**
     * 生成限流规则的key
     * @param appId 应用ID
     * @param api API路径
     * @return 组合的key
     */
    private String generateKey(String appId, String api) {
        return appId + ":" + api;
    }
}
