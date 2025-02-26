package com.geektime.ratelimiter;

import com.geektime.ratelimiter.alg.FixedTimeWinRateLimitAlg;
import com.geektime.ratelimiter.alg.RateLimitAlg;
import com.geektime.ratelimiter.rule.ApiLimit;
import com.geektime.ratelimiter.rule.RateLimitRule;
import com.geektime.ratelimiter.rule.RuleConfig;
import com.geektime.ratelimiter.rule.TrieRateLimitRule;
import com.geektime.ratelimiter.rule.datasource.FileRuleConfigSource;
import com.geektime.ratelimiter.rule.datasource.RuleConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 限流器
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:02
 **/
public class RateLimiter {
    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);
    
    // 为每个api在内存中存储限流计数器
    private final ConcurrentHashMap<String, RateLimitAlg> counters = new ConcurrentHashMap<>();
    private final RateLimitRule rule;

    public RateLimiter() {
        //调用RuleConfigSource类来实现配置加载
        RuleConfigSource configSource = new FileRuleConfigSource();
        RuleConfig ruleConfig = configSource.load();
        log.info("Loaded rate limit rules: {}", ruleConfig);
        this.rule = new TrieRateLimitRule(ruleConfig);
    }

    public boolean limit(String appId, String url) {
        ApiLimit apiLimit = rule.getLimit(appId, url);
        if (apiLimit == null) {
            log.warn("No rate limit rule found for appId: {}, url: {}", appId, url);
            return true;
        }

        // 获取api对应在内存中的限流计数器（rateLimitCounter）
        String counterKey = appId + ":" + apiLimit.getApi();
        RateLimitAlg rateLimitCounter = counters.get(counterKey);
        if (rateLimitCounter == null) {
            log.info("Creating new rate limiter for appId: {}, url: {}, limit: {}, unit: {}s", 
                    appId, url, apiLimit.getLimit(), apiLimit.getUnit());
            RateLimitAlg newRateLimitCounter = new FixedTimeWinRateLimitAlg(
                    apiLimit.getLimit(), apiLimit.getUnit() * 1000);
            rateLimitCounter = counters.putIfAbsent(counterKey, newRateLimitCounter);
            if (rateLimitCounter == null) {
                rateLimitCounter = newRateLimitCounter;
            }
        }

        // 判断是否限流
        boolean acquired = rateLimitCounter.tryAcquire();
        if (!acquired) {
            log.warn("Rate limit exceeded for appId: {}, url: {}, current count: {}", 
                    appId, url, rateLimitCounter.getCurrentCount());
        } else {
            log.debug("Request accepted for appId: {}, url: {}, current count: {}", 
                    appId, url, rateLimitCounter.getCurrentCount());
        }
        return acquired;
    }
}
