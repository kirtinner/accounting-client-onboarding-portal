package com.kzhastkou.accountingonboarding.audit.controller;

import com.kzhastkou.accountingonboarding.audit.dto.AuditLogResponse;
import com.kzhastkou.accountingonboarding.audit.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> getAuditLogs() {
        return auditLogService.getAuditLogs();
    }
}
