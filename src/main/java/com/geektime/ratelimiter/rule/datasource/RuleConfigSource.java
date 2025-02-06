package com.geektime.ratelimiter.rule.datasource;

import com.geektime.ratelimiter.rule.RuleConfig;

/**
 * @Description: 规则配置数据源接口
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public interface RuleConfigSource {
    /**
     * 加载配置
     * @return 规则配置对象
     */
    RuleConfig load();
} 