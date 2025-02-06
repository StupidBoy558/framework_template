package com.geektime.ratelimiter.rule;

/**
 * @Description: 限流规则
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
