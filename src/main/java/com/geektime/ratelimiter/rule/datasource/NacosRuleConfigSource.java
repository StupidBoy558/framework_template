package com.geektime.ratelimiter.rule.datasource;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.geektime.ratelimiter.rule.RuleConfig;
import com.geektime.ratelimiter.rule.parser.JsonRuleConfigParser;
import com.geektime.ratelimiter.rule.parser.RuleConfigParser;
import com.geektime.ratelimiter.rule.parser.YamlRuleConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @Description: 基于Nacos配置中心的规则配置数据源实现
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:04
 **/
public class NacosRuleConfigSource implements RuleConfigSource {
    private static final Logger log = LoggerFactory.getLogger(NacosRuleConfigSource.class);
    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    private static final long DEFAULT_TIMEOUT_MS = 5000;

    private final ConfigService configService;
    private final String dataId;
    private final String group;
    private final RuleConfigParser parser;
    private RuleConfig ruleConfig;

    public NacosRuleConfigSource(String serverAddr, String dataId, String fileExtension) throws NacosException {
        this(serverAddr, dataId, DEFAULT_GROUP, fileExtension);
    }

    public NacosRuleConfigSource(String serverAddr, String dataId, String group, String fileExtension) throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        this.configService = NacosFactory.createConfigService(properties);
        this.dataId = dataId;
        this.group = group;
        this.parser = createParser(fileExtension);
        
        // 初始化配置
        this.ruleConfig = load();
        
        // 注册配置变更监听器
        registerConfigChangeListener();
    }

    @Override
    public RuleConfig load() {
        try {
            String configText = configService.getConfig(dataId, group, DEFAULT_TIMEOUT_MS);
            if (configText == null || configText.isEmpty()) {
                return null;
            }
            return parser.parse(configText);
        } catch (NacosException e) {
            log.error("Failed to load config from nacos, dataId: {}, group: {}", dataId, group, e);
            throw new RuntimeException("Failed to load config from nacos", e);
        }
    }

    private void registerConfigChangeListener() {
        try {
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    try {
                        RuleConfig newRuleConfig = parser.parse(configInfo);
                        if (newRuleConfig != null) {
                            ruleConfig = newRuleConfig;
                            log.info("Config updated for dataId: {}, group: {}", dataId, group);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse updated config, dataId: {}, group: {}", dataId, group, e);
                    }
                }

                @Override
                public Executor getExecutor() {
                    return null; // 使用默认执行器
                }
            });
        } catch (Exception e) {
            log.error("Failed to register config change listener, dataId: {}, group: {}", dataId, group, e);
        }
    }

    private RuleConfigParser createParser(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            throw new IllegalArgumentException("File extension cannot be empty");
        }
        
        fileExtension = fileExtension.toLowerCase();
        switch (fileExtension) {
            case "yaml":
            case "yml":
                return new YamlRuleConfigParser();
            case "json":
                return new JsonRuleConfigParser();
            default:
                throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
        }
    }

    /**
     * 获取当前的规则配置
     * @return 当前的规则配置
     */
    public RuleConfig getCurrentRuleConfig() {
        return ruleConfig;
    }
} 