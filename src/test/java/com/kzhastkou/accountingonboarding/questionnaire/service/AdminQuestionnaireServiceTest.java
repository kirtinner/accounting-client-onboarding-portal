package com.kzhastkou.accountingonboarding.questionnaire.service;

import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.service.AuditLogService;
import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.exception.NotImplementedException;
import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.questionnaire.dto.AdminQuestionnaireUpdateRequest;
import com.kzhastkou.accountingonboarding.questionnaire.dto.AdminQuestionnaireDetailResponse;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireRequest;
import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import com.kzhastkou.accountingonboarding.questionnaire.repository.QuestionnaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdminQuestionnaireServiceTest {

    private QuestionnaireRepository repository;
    private AuditLogService auditLogService;
    private AdminQuestionnaireService service;

    @BeforeEach
    void setUp() {
        repository = mock(QuestionnaireRepository.class);
        auditLogService = mock(AuditLogService.class);
        service = new AdminQuestionnaireService(repository, auditLogService);
    }

    @Test
    void approveSubmittedQuestionnaireSucceeds() {
        Questionnaire questionnaire = questionnaire(InvitationStatus.SUBMITTED);
        when(repository.findByIdWithInvitation(1L)).thenReturn(Optional.of(questionnaire));

        AdminQuestionnaireDetailResponse response = service.approveQuestionnaire(1L);

        assertEquals(InvitationStatus.APPROVED, response.invitationStatus());
        assertNotNull(response.approvedAt());
        verify(auditLogService).recordSystemSuccess(
                AuditAction.QUESTIONNAIRE_APPROVED,
                "QUESTIONNAIRE",
                1L,
                "Questionnaire approved"
        );
    }

    @Test
    void updateQuestionnaireChangesEditableFieldsOnly() {
        Questionnaire questionnaire = questionnaire(InvitationStatus.SUBMITTED);
        Instant submittedAt = questionnaire.getSubmittedAt();
        when(repository.findByIdWithInvitation(1L)).thenReturn(Optional.of(questionnaire));

        AdminQuestionnaireDetailResponse response = service.updateQuestionnaire(1L, new AdminQuestionnaireUpdateRequest(
                ClientType.INDIVIDUAL,
                "Taylor",
                "James",
                "Brown",
                LocalDate.of(1991, 2, 3),
                "taylor@example.com",
                "0411111111",
                "2 Queen Street",
                "Level 1",
                "Sydney",
                "NSW",
                "2000",
                "Australia"
        ));

        assertEquals("Taylor", response.firstName());
        assertEquals("James", response.middleName());
        assertEquals("Brown", response.lastName());
        assertEquals("taylor@example.com", response.email());
        assertEquals(InvitationStatus.SUBMITTED, response.invitationStatus());
        assertEquals(submittedAt, response.submittedAt());
        assertNull(response.approvedAt());
        assertNull(response.xpmSentAt());
        verify(auditLogService).recordSystemSuccess(
                AuditAction.QUESTIONNAIRE_UPDATED,
                "QUESTIONNAIRE",
                1L,
                "Questionnaire updated"
        );
    }

    @Test
    void approveNonSubmittedQuestionnaireFails() {
        Questionnaire questionnaire = questionnaire(InvitationStatus.APPROVED);
        when(repository.findByIdWithInvitation(1L)).thenReturn(Optional.of(questionnaire));

        assertThrows(BadRequestException.class, () -> service.approveQuestionnaire(1L));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void reopenApprovedQuestionnaireSucceedsAndClearsApprovedAt() {
        Questionnaire questionnaire = questionnaire(InvitationStatus.SUBMITTED);
        questionnaire.getInvitation().markApproved(Instant.now());
        when(repository.findByIdWithInvitation(1L)).thenReturn(Optional.of(questionnaire));

        AdminQuestionnaireDetailResponse response = service.reopenQuestionnaire(1L);

        assertEquals(InvitationStatus.SUBMITTED, response.invitationStatus());
        assertNull(response.approvedAt());
        verify(auditLogService).recordSystemSuccess(
                AuditAction.QUESTIONNAIRE_APPROVAL_CANCELLED,
                "QUESTIONNAIRE",
                1L,
                "Questionnaire approval cancelled"
        );
    }

    @Test
    void reopenNonApprovedQuestionnaireFails() {
        Questionnaire questionnaire = questionnaire(InvitationStatus.SUBMITTED);
        when(repository.findByIdWithInvitation(1L)).thenReturn(Optional.of(questionnaire));

        assertThrows(BadRequestException.class, () -> service.reopenQuestionnaire(1L));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void sendToXpmForApprovedReturnsNotImplemented() {
        Questionnaire questionnaire = questionnaire(InvitationStatus.SUBMITTED);
        questionnaire.getInvitation().markApproved(Instant.now());
        when(repository.findByIdWithInvitation(1L)).thenReturn(Optional.of(questionnaire));

        NotImplementedException exception = assertThrows(NotImplementedException.class, () -> service.sendToXpm(1L));

        assertEquals("Xero Practice Manager integration is not available yet.", exception.getMessage());
    }

    @Test
    void sendToXpmForNonApprovedFails() {
        Questionnaire questionnaire = questionnaire(InvitationStatus.SUBMITTED);
        when(repository.findByIdWithInvitation(1L)).thenReturn(Optional.of(questionnaire));

        assertThrows(BadRequestException.class, () -> service.sendToXpm(1L));
    }

    private Questionnaire questionnaire(InvitationStatus invitationStatus) {
        Instant now = Instant.now();
        OnboardingInvitation invitation = new OnboardingInvitation(
                UUID.randomUUID(),
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL,
                now,
                null
        );
        invitation.markSent(now, Duration.ofDays(10));
        invitation.markSubmitted(now);
        Questionnaire questionnaire = new Questionnaire(invitation, request(true), now);
        ReflectionTestUtils.setField(questionnaire, "id", 1L);
        questionnaire.submit(now);
        if (invitationStatus == InvitationStatus.APPROVED) {
            invitation.markApproved(now);
        }
        return questionnaire;
    }

    private QuestionnaireRequest request(boolean clientConfirmed) {
        return new QuestionnaireRequest(
                ClientType.INDIVIDUAL,
                "Alex",
                null,
                "Smith",
                LocalDate.of(1990, 1, 1),
                "alex.client@example.com",
                "0400000000",
                "1 Collins Street",
                null,
                "Melbourne",
                "VIC",
                "3000",
                "Australia",
                clientConfirmed
        );
    }
}
