package com.kzhastkou.accountingonboarding.xero;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class XeroIntegrationService {

    private static final String AUTHORIZE_URL = "https://login.xero.com/identity/connect/authorize";
    private static final String TOKEN_URL = "https://identity.xero.com/connect/token";
    private static final String CONNECTIONS_URL = "https://api.xero.com/connections";
    private static final String CONTACTS_URL = "https://api.xero.com/api.xro/2.0/Contacts";
    private static final String SCOPES = "offline_access accounting.contacts accounting.settings.read";

    private final XeroProperties properties;
    private final XeroConnectionState connectionState;
    private final RestClient restClient;

    public XeroIntegrationService(XeroProperties properties, XeroConnectionState connectionState, RestClient restClient) {
        this.properties = properties;
        this.connectionState = connectionState;
        this.restClient = restClient;
    }

    public String buildAuthorizationUrl() {
        requireConfigured();

        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("scope", SCOPES)
                .build()
                .encode()
                .toUriString();
    }

    public XeroCallbackResponse connectWithAuthorizationCode(String code) {
        requireConfigured();

        Map<?, ?> tokenResponse = exchangeCodeForTokens(code);
        String accessToken = requireString(tokenResponse, "access_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");
        String tenantId = fetchFirstTenantId(accessToken);

        connectionState.update(accessToken, refreshToken, tenantId);

        return new XeroCallbackResponse("connected", true, tenantId);
    }

    public String getContacts() {
        if (!connectionState.isConnected()) {
            throw new BadRequestException("Xero is not connected");
        }

        return restClient.get()
                .uri(CONTACTS_URL)
                .header("Authorization", "Bearer " + connectionState.getAccessToken())
                .header("xero-tenant-id", connectionState.getTenantId())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    public XeroStatusResponse getStatus() {
        return new XeroStatusResponse(
                connectionState.isConnected(),
                connectionState.getTenantId() != null,
                connectionState.getTenantId()
        );
    }

    private Map<?, ?> exchangeCodeForTokens(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", properties.redirectUri());

        return restClient.post()
                .uri(TOKEN_URL)
                .headers(headers -> headers.setBasicAuth(properties.clientId(), properties.clientSecret()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    private String fetchFirstTenantId(String accessToken) {
        List<?> connections = restClient.get()
                .uri(CONNECTIONS_URL)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(List.class);

        if (connections == null || connections.isEmpty()) {
            throw new BadRequestException("No Xero tenant connection found");
        }

        Object firstConnection = connections.getFirst();
        if (!(firstConnection instanceof Map<?, ?> connection)) {
            throw new BadRequestException("Unexpected Xero connections response");
        }

        return requireString(connection, "tenantId");
    }

    private String requireString(Map<?, ?> source, String key) {
        Object value = source == null ? null : source.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new BadRequestException("Missing Xero response field: " + key);
        }
        return text;
    }

    private void requireConfigured() {
        if (isBlank(properties.clientId()) || isBlank(properties.clientSecret()) || isBlank(properties.redirectUri())) {
            throw new BadRequestException("Xero client-id, client-secret, and redirect-uri must be configured");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
