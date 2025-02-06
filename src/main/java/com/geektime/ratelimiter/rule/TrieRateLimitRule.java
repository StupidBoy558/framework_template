package com.geektime.ratelimiter.rule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 基于Trie树的限流规则实现类
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public class TrieRateLimitRule implements RateLimitRule {
    // 使用ConcurrentHashMap存储限流规则，key为appId:api的组合
    private final Map<String, ApiLimit> limitMap = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，初始化限流规则
     * @param ruleConfig 规则配置
     */
    public TrieRateLimitRule(RuleConfig ruleConfig) {
        // 从配置中加载限流规则
        if (ruleConfig != null && ruleConfig.getLimits() != null) {
            for (ApiLimit limit : ruleConfig.getLimits()) {
                String key = generateKey(limit.getAppId(), limit.getApi());
                limitMap.put(key, limit);
            }
        }
    }

    @Override
    public ApiLimit getLimit(String appId, String api) {
        if (appId == null || api == null) {
            return null;
        }
        return limitMap.get(generateKey(appId, api));
    }

    @Override
    public void addLimit(ApiLimit limit) {
        if (limit != null) {
            String key = generateKey(limit.getAppId(), limit.getApi());
            limitMap.put(key, limit);
        }
    }

    @Override
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