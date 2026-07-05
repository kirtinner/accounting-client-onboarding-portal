package com.kzhastkou.accountingonboarding.xero;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class XeroIntegrationService {

    private static final String AUTHORIZE_URL = "https://login.xero.com/identity/connect/authorize";
    private static final String TOKEN_URL = "https://identity.xero.com/connect/token";
    private static final String CONNECTIONS_URL = "https://api.xero.com/connections";
    private static final String CONTACTS_URL = "https://api.xero.com/api.xro/2.0/Contacts";
    private static final String ORGANISATION_URL = "https://api.xero.com/api.xro/2.0/Organisation";
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
        List<XeroConnectionResponse> connections = fetchConnections(accessToken);

        connectionState.update(accessToken, refreshToken, connections);

        return new XeroCallbackResponse("connected", true, connectionState.getSelectedTenantId());
    }

    public Map<String, Object> getContacts() {
        requireConnected();

        try {
            Map<?, ?> response = restClient.get()
                    .uri(CONTACTS_URL)
                    .header("Authorization", "Bearer " + connectionState.getAccessToken())
                    .header("xero-tenant-id", connectionState.getSelectedTenantId())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            return withSelectedTenant(response);
        } catch (RestClientResponseException exception) {
            throw toXeroApiException(exception);
        }
    }

    public Map<String, Object> createContact(CreateXeroContactRequest request) {
        requireConnected();

        Map<String, Object> contact = Map.of(
                "Name", request.name(),
                "FirstName", request.firstName(),
                "LastName", request.lastName(),
                "EmailAddress", request.email()
        );

        Map<String, Object> payload = Map.of("Contacts", List.of(contact));

        try {
            Map<?, ?> response = restClient.post()
                    .uri(CONTACTS_URL)
                    .header("Authorization", "Bearer " + connectionState.getAccessToken())
                    .header("xero-tenant-id", connectionState.getSelectedTenantId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            return withSelectedTenant(response);
        } catch (RestClientResponseException exception) {
            throw toXeroApiException(exception);
        }
    }

    public List<XeroConnectionResponse> getConnections() {
        requireConnected();
        return connectionState.getConnections();
    }

    public XeroOrganisationResponse getOrganisation() {
        requireConnected();

        try {
            Map<?, ?> response = restClient.get()
                    .uri(ORGANISATION_URL)
                    .header("Authorization", "Bearer " + connectionState.getAccessToken())
                    .header("xero-tenant-id", connectionState.getSelectedTenantId())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            Object organisationsValue = response == null ? null : response.get("Organisations");
            if (!(organisationsValue instanceof List<?> organisations) || organisations.isEmpty()) {
                throw new BadRequestException("Xero organisation response did not contain an organisation");
            }

            Object firstOrganisation = organisations.getFirst();
            if (!(firstOrganisation instanceof Map<?, ?> organisation)) {
                throw new BadRequestException("Unexpected Xero organisation response");
            }

            return new XeroOrganisationResponse(
                    optionalString(organisation, "Name"),
                    optionalString(organisation, "LegalName"),
                    optionalString(organisation, "CountryCode"),
                    optionalString(organisation, "BaseCurrency"),
                    optionalString(organisation, "OrganisationType"),
                    optionalString(organisation, "Timezone")
            );
        } catch (RestClientResponseException exception) {
            throw toXeroApiException(exception);
        }
    }

    public XeroStatusResponse getStatus() {
        return new XeroStatusResponse(
                connectionState.isConnected(),
                connectionState.getSelectedTenantId(),
                connectionState.getSelectedTenantName(),
                connectionState.getConnections().size()
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

    private List<XeroConnectionResponse> fetchConnections(String accessToken) {
        List<?> connections;
        try {
            connections = restClient.get()
                    .uri(CONNECTIONS_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(List.class);
        } catch (RestClientResponseException exception) {
            throw toXeroApiException(exception);
        }

        if (connections == null || connections.isEmpty()) {
            throw new BadRequestException("No Xero tenant connection found");
        }

        return connections.stream()
                .map(this::toConnectionResponse)
                .toList();
    }

    private String requireString(Map<?, ?> source, String key) {
        Object value = source == null ? null : source.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new BadRequestException("Missing Xero response field: " + key);
        }
        return text;
    }

    private XeroConnectionResponse toConnectionResponse(Object connectionValue) {
        if (!(connectionValue instanceof Map<?, ?> connection)) {
            throw new BadRequestException("Unexpected Xero connections response");
        }

        return new XeroConnectionResponse(
                requireString(connection, "tenantId"),
                requireString(connection, "tenantName"),
                optionalString(connection, "tenantType")
        );
    }

    private Map<String, Object> withSelectedTenant(Map<?, ?> xeroResponse) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tenantId", connectionState.getSelectedTenantId());
        response.put("tenantName", connectionState.getSelectedTenantName());

        if (xeroResponse != null) {
            xeroResponse.forEach((key, value) -> response.put(key.toString(), value));
        }

        return response;
    }

    private String optionalString(Map<?, ?> source, String key) {
        Object value = source == null ? null : source.get(key);
        return value == null ? null : value.toString();
    }

    private void requireConfigured() {
        if (isBlank(properties.clientId()) || isBlank(properties.clientSecret()) || isBlank(properties.redirectUri())) {
            throw new BadRequestException("Xero client-id, client-secret, and redirect-uri must be configured");
        }
    }

    private void requireConnected() {
        if (!connectionState.isConnected()) {
            throw new BadRequestException("Xero is not connected");
        }
    }

    private BadRequestException toXeroApiException(RestClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        String message = responseBody == null || responseBody.isBlank()
                ? exception.getStatusText()
                : responseBody;
        return new BadRequestException("Xero API request failed with status "
                + exception.getStatusCode().value() + ": " + message);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
