package com.kzhastkou.accountingonboarding.audit.service;

import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.entity.AuditLog;
import com.kzhastkou.accountingonboarding.audit.entity.AuditResult;
import com.kzhastkou.accountingonboarding.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogServiceTest {

    @Test
    void recordSystemSuccessSavesAuditLogWithExpectedValues() {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogService service = new AuditLogService(repository);
        when(repository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Instant before = Instant.now();

        service.recordSystemSuccess(
                AuditAction.QUESTIONNAIRE_APPROVED,
                "QUESTIONNAIRE",
                10L,
                "Questionnaire approved"
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog auditLog = captor.getValue();
        assertNotNull(auditLog.getOccurredAt());
        assertFalse(auditLog.getOccurredAt().isBefore(before));
        assertEquals(AuditLogService.SYSTEM_ACTOR, auditLog.getActor());
        assertEquals(AuditAction.QUESTIONNAIRE_APPROVED, auditLog.getAction());
        assertEquals("QUESTIONNAIRE", auditLog.getEntityType());
        assertEquals(10L, auditLog.getEntityId());
        assertEquals(AuditResult.SUCCESS, auditLog.getResult());
        assertEquals("Questionnaire approved", auditLog.getDescription());
    }
}
