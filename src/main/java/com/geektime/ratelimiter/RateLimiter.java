package com.geektime.ratelimiter;

import com.geektime.ratelimiter.alg.FixedTimeWinRateLimitAlg;
import com.geektime.ratelimiter.alg.RateLimitAlg;
import com.geektime.ratelimiter.rule.ApiLimit;
import com.geektime.ratelimiter.rule.RateLimitRule;
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
        // 从配置文件加载限流规则
        RuleConfigSource configSource = new FileRuleConfigSource("/ratelimiter-rule.yaml");
        rule = new TrieRateLimitRule(configSource.load());
    }

    public boolean limit(String appId, String url) throws Exception {
        ApiLimit apiLimit = rule.getLimit(appId, url);
        if (apiLimit == null) {
            return true;
        }

        // 获取api对应在内存中的限流计数器（rateLimitCounter）
        String counterKey = appId + ":" + apiLimit.getApi();
        RateLimitAlg rateLimitCounter = counters.get(counterKey);
        if (rateLimitCounter == null) {
            RateLimitAlg newRateLimitCounter = new FixedTimeWinRateLimitAlg(
                    apiLimit.getLimit(), apiLimit.getUnit() * 1000);
            rateLimitCounter = counters.putIfAbsent(counterKey, newRateLimitCounter);
            if (rateLimitCounter == null) {
                rateLimitCounter = newRateLimitCounter;
            }
        }

        // 判断是否限流
        return rateLimitCounter.tryAcquire();
    }
}
