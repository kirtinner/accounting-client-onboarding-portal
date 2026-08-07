package com.kzhastkou.accountingonboarding.audit.service;

import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.entity.AuditLog;
import com.kzhastkou.accountingonboarding.audit.entity.AuditResult;
import com.kzhastkou.accountingonboarding.audit.dto.AuditLogResponse;
import com.kzhastkou.accountingonboarding.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuditLogService {

    public static final String SYSTEM_ACTOR = "SYSTEM";

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog recordSystemSuccess(AuditAction action, String entityType, Long entityId, String description) {
        return record(SYSTEM_ACTOR, action, entityType, entityId, AuditResult.SUCCESS, description);
    }

    public AuditLog record(String actor, AuditAction action, String entityType, Long entityId,
                           AuditResult result, String description) {
        AuditLog auditLog = new AuditLog(
                Instant.now(),
                actor,
                action,
                entityType,
                entityId,
                result,
                description
        );
        return auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs() {
        return auditLogRepository.findAllByOrderByOccurredAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getOccurredAt(),
                auditLog.getActor(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getResult(),
                auditLog.getDescription()
        );
    }
}
