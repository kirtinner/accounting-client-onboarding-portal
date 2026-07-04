package com.kzhastkou.accountingonboarding.xero;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xero")
public record XeroProperties(
        String clientId,
        String clientSecret,
        String redirectUri
) {
}
