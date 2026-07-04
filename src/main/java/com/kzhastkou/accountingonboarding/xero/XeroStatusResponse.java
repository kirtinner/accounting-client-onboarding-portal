package com.kzhastkou.accountingonboarding.xero;

public record XeroStatusResponse(
        boolean connected,
        boolean hasTenantId,
        String tenantId
) {
}
