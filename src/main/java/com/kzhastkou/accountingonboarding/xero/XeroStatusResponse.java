package com.kzhastkou.accountingonboarding.xero;

public record XeroStatusResponse(
        boolean connected,
        String selectedTenantId,
        String selectedTenantName,
        int availableConnections
) {
}
