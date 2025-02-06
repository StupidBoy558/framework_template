package com.geektime.ratelimiter.rule;

import java.util.List;

/**
 * @Description: 限流规则配置
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

