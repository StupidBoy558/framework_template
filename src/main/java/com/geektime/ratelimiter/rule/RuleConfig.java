package com.geektime.ratelimiter.rule;

import java.util.List;

/**
 * @Description: 限流规则配置
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:03
 **/

public class RuleConfig {
    private List<AppRuleConfig> configs;

    public List<AppRuleConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<AppRuleConfig> configs) {
        this.configs = configs;
    }

    public static class AppRuleConfig {
        private String appId;
        private List<ApiLimit> limits;

        public AppRuleConfig() {}

        public AppRuleConfig(String appId, List<ApiLimit> limits) {
            this.appId = appId;
            this.limits = limits;
        }

        public List<ApiLimit> getLimits() {
            return limits;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public void setLimits(List<ApiLimit> limits) {
            this.limits = limits;
        }
    }
}

