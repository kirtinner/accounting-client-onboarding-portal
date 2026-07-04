package com.kzhastkou.accountingonboarding.xero;

import org.springframework.stereotype.Component;

@Component
public class XeroConnectionState {

    private String accessToken;
    private String refreshToken;
    private String tenantId;

    public boolean isConnected() {
        return accessToken != null && tenantId != null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void update(String accessToken, String refreshToken, String tenantId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tenantId = tenantId;
    }
}
