package com.kzhastkou.accountingonboarding.xero;

public record XeroCallbackResponse(
        String status,
        boolean connected,
        String tenantId
) {
}
