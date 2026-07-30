package com.kzhastkou.accountingonboarding.questionnaire.service;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.exception.NotFoundException;
import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.invitation.repository.OnboardingInvitationRepository;
import com.kzhastkou.accountingonboarding.questionnaire.dto.PublicOnboardingResponse;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicQuestionnaireServiceTest {

    private static final Duration INVITATION_VALIDITY = Duration.ofDays(10);

    private OnboardingInvitationRepository invitationRepository;
    private QuestionnaireRepository questionnaireRepository;
    private PublicQuestionnaireService service;

    @BeforeEach
    void setUp() {
        invitationRepository = mock(OnboardingInvitationRepository.class);
        questionnaireRepository = mock(QuestionnaireRepository.class);
        QuestionnaireService questionnaireService = new QuestionnaireService(questionnaireRepository);
        service = new PublicQuestionnaireService(invitationRepository, questionnaireRepository, questionnaireService);

        when(questionnaireRepository.save(any(Questionnaire.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getValidSentInvitationReturnsContext() {
        OnboardingInvitation invitation = sentInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.empty());

        PublicOnboardingResponse response = service.getOnboarding(invitation.getToken().toString());

        assertEquals(invitation.getToken(), response.token());
        assertEquals("Alex Smith", response.preferredName());
        assertEquals("alex@example.com", response.email());
        assertEquals(ClientType.INDIVIDUAL, response.clientType());
        assertEquals(invitation.getExpiresAt(), response.expiresAt());
        assertNull(response.questionnaire());
    }

    @Test
    void getInvalidTokenReturnsNotFound() {
        UUID token = UUID.randomUUID();
        when(invitationRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getOnboarding(token.toString()));
    }

    @Test
    void putCreatesQuestionnaireForSentInvitation() {
        OnboardingInvitation invitation = sentInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.empty());

        QuestionnaireResponse response = service.createOrUpdateQuestionnaire(invitation.getToken().toString(), request(true));

        assertEquals("Alex", response.firstName());
        assertEquals("Smith", response.lastName());
        assertEquals("alex@example.com", response.email());
        assertEquals(true, response.clientConfirmed());
    }

    @Test
    void putRejectedForDraftInvitation() {
        OnboardingInvitation invitation = draftInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        assertThrows(BadRequestException.class,
                () -> service.createOrUpdateQuestionnaire(invitation.getToken().toString(), request(false)));
    }

    @Test
    void putRejectedForExpiredSentInvitation() {
        OnboardingInvitation invitation = expiredSentInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        assertThrows(BadRequestException.class,
                () -> service.createOrUpdateQuestionnaire(invitation.getToken().toString(), request(false)));
    }

    @Test
    void postSubmitChangesStatusToSubmitted() {
        OnboardingInvitation invitation = sentInvitation();
        Questionnaire questionnaire = new Questionnaire(invitation, request(true), Instant.now());
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.of(questionnaire));

        QuestionnaireResponse response = service.submitQuestionnaire(invitation.getToken().toString());

        assertEquals(InvitationStatus.SUBMITTED, invitation.getStatus());
        assertNotNull(invitation.getSubmittedAt());
        assertNotNull(response.submittedAt());
    }

    @Test
    void postSubmitWithoutQuestionnaireFails() {
        OnboardingInvitation invitation = sentInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.submitQuestionnaire(invitation.getToken().toString()));
    }

    @Test
    void postSubmitWithoutClientConfirmedFails() {
        OnboardingInvitation invitation = sentInvitation();
        Questionnaire questionnaire = new Questionnaire(invitation, request(false), Instant.now());
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.of(questionnaire));

        assertThrows(BadRequestException.class, () -> service.submitQuestionnaire(invitation.getToken().toString()));
    }

    private OnboardingInvitation draftInvitation() {
        return new OnboardingInvitation(
                UUID.randomUUID(),
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL,
                Instant.now(),
                null
        );
    }

    private OnboardingInvitation sentInvitation() {
        OnboardingInvitation invitation = draftInvitation();
        invitation.markSent(Instant.now(), INVITATION_VALIDITY);
        return invitation;
    }

    private OnboardingInvitation expiredSentInvitation() {
        OnboardingInvitation invitation = new OnboardingInvitation(
                UUID.randomUUID(),
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL,
                Instant.now().minus(Duration.ofDays(12)),
                null
        );
        invitation.markSent(
                Instant.now().minus(Duration.ofDays(11)),
                Duration.ofDays(10)
        );
        return invitation;
    }

    private QuestionnaireRequest request(boolean clientConfirmed) {
        return new QuestionnaireRequest(
                ClientType.INDIVIDUAL,
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
}
