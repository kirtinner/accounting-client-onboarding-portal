package com.kzhastkou.accountingonboarding.audit.dto;

import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.entity.AuditResult;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Instant occurredAt,
        String actor,
        AuditAction action,
        String entityType,
        Long entityId,
        AuditResult result,
        String description
) {
}
