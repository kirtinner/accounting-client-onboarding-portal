package com.kzhastkou.accountingonboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AccountingClientOnboardingPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountingClientOnboardingPortalApplication.class, args);
    }

}
