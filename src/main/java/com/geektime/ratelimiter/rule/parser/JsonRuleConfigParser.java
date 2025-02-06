package com.geektime.ratelimiter.rule.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geektime.ratelimiter.rule.RuleConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Description: JSON格式规则配置解析器
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public class JsonRuleConfigParser implements RuleConfigParser {
    private final ObjectMapper objectMapper;

    public JsonRuleConfigParser() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public RuleConfig parse(InputStream in) {
        if (in == null) {
            return null;
        }
        try {
            return objectMapper.readValue(in, RuleConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Parse json rule config error", e);
        }
    }
} 