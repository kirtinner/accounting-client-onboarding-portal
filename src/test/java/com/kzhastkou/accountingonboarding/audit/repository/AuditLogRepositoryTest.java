package com.kzhastkou.accountingonboarding.audit.repository;

import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.entity.AuditLog;
import com.kzhastkou.accountingonboarding.audit.entity.AuditResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void savesAndReadsAuditLog() {
        Instant occurredAt = Instant.parse("2026-08-02T10:15:30Z");
        AuditLog auditLog = new AuditLog(
                occurredAt,
                "admin",
                AuditAction.INVITATION_SENT,
                "OnboardingInvitation",
                42L,
                AuditResult.SUCCESS,
                "Invitation sent"
        );

        AuditLog saved = auditLogRepository.saveAndFlush(auditLog);

        AuditLog found = auditLogRepository.findById(saved.getId()).orElseThrow();
        assertNotNull(found.getId());
        assertEquals(occurredAt, found.getOccurredAt());
        assertEquals("admin", found.getActor());
        assertEquals(AuditAction.INVITATION_SENT, found.getAction());
        assertEquals("OnboardingInvitation", found.getEntityType());
        assertEquals(42L, found.getEntityId());
        assertEquals(AuditResult.SUCCESS, found.getResult());
        assertEquals("Invitation sent", found.getDescription());
    }
}
