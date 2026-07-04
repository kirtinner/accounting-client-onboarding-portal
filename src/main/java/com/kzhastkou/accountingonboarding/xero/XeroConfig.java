package com.kzhastkou.accountingonboarding.xero;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(XeroProperties.class)
public class XeroConfig {

    @Bean
    RestClient restClient() {
        return RestClient.create();
    }
}
