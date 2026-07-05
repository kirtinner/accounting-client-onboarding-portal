package com.kzhastkou.accountingonboarding.xero;

public record XeroOrganisationResponse(
        String name,
        String legalName,
        String countryCode,
        String baseCurrency,
        String organisationType,
        String timezone
) {
}
