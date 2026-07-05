package com.kzhastkou.accountingonboarding.xero;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/xero")
public class XeroController {

    private final XeroIntegrationService xeroIntegrationService;

    public XeroController(XeroIntegrationService xeroIntegrationService) {
        this.xeroIntegrationService = xeroIntegrationService;
    }

    @GetMapping("/connect")
    public void connect(HttpServletResponse response) throws IOException {
        response.sendRedirect(xeroIntegrationService.buildAuthorizationUrl());
    }

    @GetMapping("/callback")
    public XeroCallbackResponse callback(@RequestParam String code) {
        return xeroIntegrationService.connectWithAuthorizationCode(code);
    }

    @GetMapping("/contacts")
    public Map<String, Object> contacts() {
        return xeroIntegrationService.getContacts();
    }

    @PostMapping("/contacts")
    public Map<String, Object> createContact(@Valid @RequestBody CreateXeroContactRequest request) {
        return xeroIntegrationService.createContact(request);
    }

    @GetMapping("/connections")
    public List<XeroConnectionResponse> connections() {
        return xeroIntegrationService.getConnections();
    }

    @GetMapping("/organisation")
    public XeroOrganisationResponse organisation() {
        return xeroIntegrationService.getOrganisation();
    }

    @GetMapping("/status")
    public XeroStatusResponse status() {
        return xeroIntegrationService.getStatus();
    }
}
