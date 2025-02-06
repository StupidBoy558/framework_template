package com.geektime.ratelimiter.rule.datasource;

import com.geektime.ratelimiter.rule.RuleConfig;
import com.geektime.ratelimiter.rule.parser.JsonRuleConfigParser;
import com.geektime.ratelimiter.rule.parser.RuleConfigParser;
import com.geektime.ratelimiter.rule.parser.YamlRuleConfigParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 基于文件的规则配置数据源实现
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public class FileRuleConfigSource implements RuleConfigSource {
    private static final String YAML_EXTENSION = "yaml";
    private static final String YML_EXTENSION = "yml";
    private static final String JSON_EXTENSION = "json";
    
    private static final Map<String, RuleConfigParser> PARSER_MAP = new HashMap<>();
    
    static {
        PARSER_MAP.put(YAML_EXTENSION, new YamlRuleConfigParser());
        PARSER_MAP.put(YML_EXTENSION, new YamlRuleConfigParser());
        PARSER_MAP.put(JSON_EXTENSION, new JsonRuleConfigParser());
    }

    private final String configFile;

    public FileRuleConfigSource(String configFile) {
        this.configFile = configFile;
    }

    public FileRuleConfigSource() {
        this("ratelimiter-rule.yaml");
    }

    @Override
    public RuleConfig load() {
        String fileExtension = getFileExtension(configFile);
        RuleConfigParser parser = PARSER_MAP.get(fileExtension);
        if (parser == null) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
        }

        try (InputStream in = this.getClass().getResourceAsStream(configFile)) {
            if (in == null) {
                throw new IllegalArgumentException("Config file not found: " + configFile);
            }
            return parser.parse(in);
        } catch (IOException e) {
            throw new RuntimeException("Read config file error", e);
        }
    }

    private String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return filePath.substring(dotIndex + 1).toLowerCase();
    }
} 