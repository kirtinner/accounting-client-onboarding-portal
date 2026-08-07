package com.kzhastkou.accountingonboarding.audit.service;

import com.kzhastkou.accountingonboarding.audit.dto.AuditLogResponse;
import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.entity.AuditLog;
import com.kzhastkou.accountingonboarding.audit.entity.AuditResult;
import com.kzhastkou.accountingonboarding.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

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

    @Test
    void getAuditLogsReturnsMappedResponsesInRepositoryOrder() {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogService service = new AuditLogService(repository);
        AuditLog newest = new AuditLog(
                Instant.parse("2026-08-04T10:15:30Z"),
                AuditLogService.SYSTEM_ACTOR,
                AuditAction.QUESTIONNAIRE_APPROVED,
                "QUESTIONNAIRE",
                20L,
                AuditResult.SUCCESS,
                "Questionnaire approved"
        );
        ReflectionTestUtils.setField(newest, "id", 2L);
        AuditLog oldest = new AuditLog(
                Instant.parse("2026-08-02T10:15:30Z"),
                "admin",
                AuditAction.INVITATION_SENT,
                "INVITATION",
                10L,
                AuditResult.SUCCESS,
                "Invitation sent"
        );
        ReflectionTestUtils.setField(oldest, "id", 1L);
        when(repository.findAllByOrderByOccurredAtDesc()).thenReturn(List.of(newest, oldest));

        List<AuditLogResponse> responses = service.getAuditLogs();

        verify(repository).findAllByOrderByOccurredAtDesc();
        assertEquals(2, responses.size());
        assertEquals(2L, responses.get(0).id());
        assertEquals(Instant.parse("2026-08-04T10:15:30Z"), responses.get(0).occurredAt());
        assertEquals(AuditLogService.SYSTEM_ACTOR, responses.get(0).actor());
        assertEquals(AuditAction.QUESTIONNAIRE_APPROVED, responses.get(0).action());
        assertEquals("QUESTIONNAIRE", responses.get(0).entityType());
        assertEquals(20L, responses.get(0).entityId());
        assertEquals(AuditResult.SUCCESS, responses.get(0).result());
        assertEquals("Questionnaire approved", responses.get(0).description());
        assertEquals(1L, responses.get(1).id());
        assertEquals("Invitation sent", responses.get(1).description());
    }
}
