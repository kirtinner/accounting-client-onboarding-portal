package com.kzhastkou.accountingonboarding.audit.controller;

import com.kzhastkou.accountingonboarding.audit.dto.AuditLogResponse;
import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.entity.AuditResult;
import com.kzhastkou.accountingonboarding.audit.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditLogControllerTest {

    @Test
    void getAuditLogsReturnsAuditLogResponses() throws Exception {
        AuditLogService auditLogService = mock(AuditLogService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AuditLogController(auditLogService))
                .build();
        when(auditLogService.getAuditLogs()).thenReturn(List.of(
                new AuditLogResponse(
                        2L,
                        Instant.parse("2026-08-04T10:15:30Z"),
                        AuditLogService.SYSTEM_ACTOR,
                        AuditAction.QUESTIONNAIRE_APPROVED,
                        "QUESTIONNAIRE",
                        20L,
                        AuditResult.SUCCESS,
                        "Questionnaire approved"
                ),
                new AuditLogResponse(
                        1L,
                        Instant.parse("2026-08-02T10:15:30Z"),
                        "admin",
                        AuditAction.INVITATION_SENT,
                        "INVITATION",
                        10L,
                        AuditResult.SUCCESS,
                        "Invitation sent"
                )
        ));

        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].occurredAt").value("2026-08-04T10:15:30Z"))
                .andExpect(jsonPath("$[0].actor").value(AuditLogService.SYSTEM_ACTOR))
                .andExpect(jsonPath("$[0].action").value("QUESTIONNAIRE_APPROVED"))
                .andExpect(jsonPath("$[0].entityType").value("QUESTIONNAIRE"))
                .andExpect(jsonPath("$[0].entityId").value(20))
                .andExpect(jsonPath("$[0].result").value("SUCCESS"))
                .andExpect(jsonPath("$[0].description").value("Questionnaire approved"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].occurredAt").value("2026-08-02T10:15:30Z"))
                .andExpect(jsonPath("$[1].actor").value("admin"))
                .andExpect(jsonPath("$[1].action").value("INVITATION_SENT"))
                .andExpect(jsonPath("$[1].entityType").value("INVITATION"))
                .andExpect(jsonPath("$[1].entityId").value(10))
                .andExpect(jsonPath("$[1].result").value("SUCCESS"))
                .andExpect(jsonPath("$[1].description").value("Invitation sent"));
        verify(auditLogService).getAuditLogs();
    }
}
