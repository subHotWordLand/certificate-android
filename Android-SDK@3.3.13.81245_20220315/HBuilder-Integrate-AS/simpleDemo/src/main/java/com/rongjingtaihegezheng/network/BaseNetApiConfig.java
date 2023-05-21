package com.rongjingtaihegezheng.network;
import com.rongjingtaihegezheng.cert.BuildConfig;

public class BaseNetApiConfig {
    public String token;
    private String sbaseDomain;

    public String getBaseDomain() {
        if (sbaseDomain != null) {
            return sbaseDomain;
        }
        Boolean buildRelease = BuildConfig.ENVIRONMENT;
        String result = buildRelease ? "https://silian-es.cqlyy.com" : "https://silian-es.1000fun.com";
        sbaseDomain = result;
        return result;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private static class BaseNetApiConfigHolder {
        private static final BaseNetApiConfig INSTANCE = new BaseNetApiConfig();
    }

    private BaseNetApiConfig() {
    }

    public static final BaseNetApiConfig getInstance() {
        return BaseNetApiConfigHolder.INSTANCE;
    }
}
