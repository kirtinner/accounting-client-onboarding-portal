package com.kzhastkou.accountingonboarding.questionnaire.service;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.invitation.entity.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireRequest;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireResponse;
import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import com.kzhastkou.accountingonboarding.questionnaire.repository.QuestionnaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionnaireServiceTest {

    private QuestionnaireRepository repository;
    private QuestionnaireService service;

    @BeforeEach
    void setUp() {
        repository = mock(QuestionnaireRepository.class);
        service = new QuestionnaireService(repository);

        when(repository.save(any(Questionnaire.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createQuestionnaireForSentInvitationSucceeds() {
        OnboardingInvitation invitation = sentInvitation();
        when(repository.findByInvitationId(invitation.getId())).thenReturn(Optional.empty());

        QuestionnaireResponse response = service.createOrUpdateForInvitation(invitation, request(true));

        assertEquals("Alex", response.firstName());
        assertEquals("Smith", response.lastName());
        assertEquals("alex@example.com", response.email());
        assertEquals("Australia", response.country());
        assertEquals(true, response.clientConfirmed());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
    }

    @Test
    void updateExistingQuestionnaireForSentInvitationSucceeds() {
        OnboardingInvitation invitation = sentInvitation();
        Questionnaire questionnaire = new Questionnaire(invitation, request(false), Instant.now());
        when(repository.findByInvitationId(invitation.getId())).thenReturn(Optional.of(questionnaire));

        QuestionnaireResponse response = service.createOrUpdateForInvitation(invitation, updatedRequest(true));

        assertEquals("Taylor", response.firstName());
        assertEquals("Brown", response.lastName());
        assertEquals("taylor@example.com", response.email());
        assertEquals("3000", response.postcode());
        assertEquals(true, response.clientConfirmed());
    }

    @Test
    void createOrUpdateForDraftInvitationFails() {
        OnboardingInvitation invitation = draftInvitation();

        assertThrows(BadRequestException.class, () -> service.createOrUpdateForInvitation(invitation, request(false)));
    }

    @Test
    void submitWithConfirmationChangesInvitationStatusToSubmitted() {
        OnboardingInvitation invitation = sentInvitation();
        Questionnaire questionnaire = new Questionnaire(invitation, request(true), Instant.now());
        when(repository.findByInvitationId(invitation.getId())).thenReturn(Optional.of(questionnaire));

        QuestionnaireResponse response = service.submitForInvitation(invitation);

        assertEquals(InvitationStatus.SUBMITTED, invitation.getStatus());
        assertNotNull(invitation.getSubmittedAt());
        assertNotNull(response.submittedAt());
        assertEquals(true, response.clientConfirmed());
    }

    @Test
    void submitWithoutConfirmationFails() {
        OnboardingInvitation invitation = sentInvitation();
        Questionnaire questionnaire = new Questionnaire(invitation, request(false), Instant.now());
        when(repository.findByInvitationId(invitation.getId())).thenReturn(Optional.of(questionnaire));

        assertThrows(BadRequestException.class, () -> service.submitForInvitation(invitation));
    }

    private OnboardingInvitation draftInvitation() {
        return new OnboardingInvitation(
                UUID.randomUUID(),
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL,
                InvitationStatus.DRAFT,
                Instant.now(),
                Instant.now().plus(Duration.ofDays(10)),
                null
        );
    }

    private OnboardingInvitation sentInvitation() {
        OnboardingInvitation invitation = draftInvitation();
        invitation.markSent(Instant.now());
        return invitation;
    }

    private QuestionnaireRequest request(boolean clientConfirmed) {
        return new QuestionnaireRequest(
                "Alex",
                null,
                "Smith",
                LocalDate.of(1990, 1, 15),
                "alex@example.com",
                "0400000000",
                "1 Main Street",
                null,
                "Sydney",
                "NSW",
                "2000",
                "Australia",
                clientConfirmed
        );
    }

    private QuestionnaireRequest updatedRequest(boolean clientConfirmed) {
        return new QuestionnaireRequest(
                "Taylor",
                "Lee",
                "Brown",
                LocalDate.of(1988, 5, 20),
                "taylor@example.com",
                "0499999999",
                "2 Collins Street",
                "Level 3",
                "Melbourne",
                "VIC",
                "3000",
                "Australia",
                clientConfirmed
        );
    }
}
