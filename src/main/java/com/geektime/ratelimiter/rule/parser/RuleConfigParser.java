package com.geektime.ratelimiter.rule.parser;

import com.geektime.ratelimiter.rule.RuleConfig;

import java.io.InputStream;

/**
 * @Description: 规则配置解析器接口
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public interface RuleConfigParser {
    /**
     * 解析配置文件
     * @param in 配置文件输入流
     * @return 规则配置对象
     */
    RuleConfig parse(InputStream in);

    /**
     * 解析配置文本
     * @param configText 配置文本
     * @return 规则配置对象
     */
    RuleConfig parse(String configText);
} 