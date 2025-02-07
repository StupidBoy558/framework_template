package com.geektime.ratelimiter.rule;

/**
 * @Description: API限流规则配置类
 * 该类定义了单个API的限流规则，包含以下属性：
 * - appId: 应用标识，用于区分不同的调用方
 * - api: API路径，用于标识具体的接口
 * - limit: 时间窗口内允许的最大请求数
 * - unit: 时间窗口大小，以秒为单位
 * 
 * 使用示例：
 * ApiLimit limit = new ApiLimit("app1", "/api/v1/user", 100, 1); // 每秒限制100次请求
 * 
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:03
 **/
public class ApiLimit {
    private String appId;
    private String api;
    private int limit;
    private long unit; // 时间单位，以秒为单位

    public ApiLimit(String appId, String api, int limit, long unit) {
        this.appId = appId;
        this.api = api;
        this.limit = limit;
        this.unit = unit;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getUnit() {
        return unit;
    }

    public void setUnit(long unit) {
        this.unit = unit;
    }
}
