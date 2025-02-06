package com.geektime.ratelimiter.rule;

import java.util.List;

/**
 * @Description: 限流规则配置管理类
 * 该类用于管理多个API的限流规则配置。
 * 通过List<ApiLimit>存储所有API的限流规则，支持批量配置。
 * 
 * 使用示例：
 * RuleConfig config = new RuleConfig();
 * List<ApiLimit> limits = new ArrayList<>();
 * limits.add(new ApiLimit("app1", "/api/v1/user", 100, 1));
 * limits.add(new ApiLimit("app1", "/api/v1/order", 50, 1));
 * config.setLimits(limits);
 * 
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:03
 **/

public class RuleConfig {
    private List<ApiLimit> limits;

    public List<ApiLimit> getLimits() {
        return limits;
    }

    public void setLimits(List<ApiLimit> limits) {
        this.limits = limits;
    }
}

