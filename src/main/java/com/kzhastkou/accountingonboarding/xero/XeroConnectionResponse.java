package com.kzhastkou.accountingonboarding.xero;

public record XeroConnectionResponse(
        String tenantId,
        String tenantName,
        String tenantType
) {
}
