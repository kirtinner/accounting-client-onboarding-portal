package com.kzhastkou.accountingonboarding.xero;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class XeroConnectionState {

    private String accessToken;
    private String refreshToken;
    private String selectedTenantId;
    private String expectedOAuthState;
    private List<XeroConnectionResponse> connections = List.of();

    public boolean isConnected() {
        return accessToken != null && selectedTenantId != null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getSelectedTenantId() {
        return selectedTenantId;
    }

    public String getSelectedTenantName() {
        return connections.stream()
                .filter(connection -> connection.tenantId().equals(selectedTenantId))
                .map(XeroConnectionResponse::tenantName)
                .findFirst()
                .orElse(null);
    }

    public List<XeroConnectionResponse> getConnections() {
        return connections;
    }

    public Optional<XeroConnectionResponse> getSelectedConnection() {
        return connections.stream()
                .filter(connection -> connection.tenantId().equals(selectedTenantId))
                .findFirst();
    }

    public synchronized void setExpectedOAuthState(String expectedOAuthState) {
        this.expectedOAuthState = expectedOAuthState;
    }

    public synchronized boolean consumeExpectedOAuthState(String state) {
        boolean valid = expectedOAuthState != null && expectedOAuthState.equals(state);
        expectedOAuthState = null;
        return valid;
    }

    public synchronized void update(String accessToken, String refreshToken, List<XeroConnectionResponse> connections) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.connections = List.copyOf(connections);
        this.selectedTenantId = connections.isEmpty() ? null : connections.getFirst().tenantId();
    }
}
