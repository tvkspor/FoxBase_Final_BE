package com.be.java.foxbase.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

@Configuration
@Getter
@Setter
public class ZaloPayConfig {
    
    @Value("${zalopay.app-id:}")
    private String appId;
    
    @Value("${zalopay.key1:}")
    private String key1;
    
    @Value("${zalopay.key2:}")
    private String key2;
    
    @Value("${zalopay.sandbox:true}")
    private Boolean sandbox;
    
    @Value("${zalopay.webhook.allowed-ips:}")
    private String allowedIps;
    
    @PostConstruct
    public void validateConfig() {
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(key1)) {
            throw new IllegalStateException("ZaloPay app-id and key1 must be configured via environment variables or application properties");
        }
    }
    
    public int getAppIdInt() {
        return Integer.parseInt(appId);
    }
    
    public boolean isIpAllowed(String ip) {
        if (!StringUtils.hasText(allowedIps)) {
            return true;
        }
        String[] allowed = allowedIps.split(",");
        for (String allowedIp : allowed) {
            if (allowedIp.trim().equals(ip)) {
                return true;
            }
        }
        return false;
    }
    
    public String getCreateOrderEndpoint() {
        if (sandbox) {
            return "https://sandbox.zalopay.com.vn/v001/tpe/createorder";
        }
        return "https://openapi.zalopay.vn/v001/tpe/createorder";
    }
    
    public String getStatusQueryEndpoint() {
        if (sandbox) {
            return "https://sandbox.zalopay.com.vn/v001/tpe/getstatusbyapptransid";
        }
        return "https://openapi.zalopay.vn/v001/tpe/getstatusbyapptransid";
    }
}

