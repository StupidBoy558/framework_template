package com.geektime.ratelimiter.rule.parser;

import com.geektime.ratelimiter.rule.RuleConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * @Description: YAML格式规则配置解析器
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public class YamlRuleConfigParser implements RuleConfigParser {
    @Override
    public RuleConfig parse(InputStream in) {
        if (in == null) {
            return null;
        }
        Yaml yaml = new Yaml();
        return yaml.loadAs(in, RuleConfig.class);
    }
} 