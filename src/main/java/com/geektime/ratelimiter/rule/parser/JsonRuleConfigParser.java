package com.geektime.ratelimiter.rule.parser;

import com.alibaba.fastjson2.JSON;
import com.geektime.ratelimiter.rule.RuleConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @Description: JSON格式规则配置解析器
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public class JsonRuleConfigParser implements RuleConfigParser {

    @Override
    public RuleConfig parse(InputStream in) {
        if (in == null) {
            return null;
        }
        try {
            byte[] bytes = in.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            return JSON.parseObject(content, RuleConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Parse json rule config error", e);
        }
    }

    @Override
    public RuleConfig parse(String configText) {
        if (configText == null || configText.isEmpty()) {
            return null;
        }
        return JSON.parseObject(configText, RuleConfig.class);
    }
} 