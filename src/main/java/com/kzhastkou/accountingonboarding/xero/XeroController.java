package com.kzhastkou.accountingonboarding.xero;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
    public String contacts() {
        return xeroIntegrationService.getContacts();
    }

    @GetMapping("/status")
    public XeroStatusResponse status() {
        return xeroIntegrationService.getStatus();
    }
}
