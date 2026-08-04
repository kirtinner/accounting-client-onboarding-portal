package com.kzhastkou.accountingonboarding.questionnaire.service;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.exception.NotFoundException;
import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.invitation.repository.OnboardingInvitationRepository;
import com.kzhastkou.accountingonboarding.questionnaire.dto.PublicOnboardingResponse;
import com.kzhastkou.accountingonboarding.questionnaire.dto.PublicQuestionnaireRequest;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireResponse;
import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import com.kzhastkou.accountingonboarding.questionnaire.exception.PublicOnboardingUnavailableException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    void getValidSentInvitationReturnsExistingQuestionnaireDraft() {
        OnboardingInvitation invitation = sentInvitation();
        Questionnaire questionnaire = new Questionnaire(invitation, partialRequest(), Instant.now());
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.of(questionnaire));

        PublicOnboardingResponse response = service.getOnboarding(invitation.getToken().toString());

        assertNotNull(response.questionnaire());
        assertEquals("Alex", response.questionnaire().firstName());
        assertEquals("Smith", response.questionnaire().lastName());
        assertNull(response.questionnaire().email());
        assertEquals(ClientType.INDIVIDUAL, response.questionnaire().clientType());
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
        assertEquals(ClientType.INDIVIDUAL, response.clientType());
        assertNotNull(response.clientType());
    }

    @Test
    void putCreatesIncompleteQuestionnaireDraftForSentInvitation() {
        OnboardingInvitation invitation = sentInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.empty());

        QuestionnaireResponse response = service.createOrUpdateQuestionnaire(
                invitation.getToken().toString(),
                partialRequest()
        );

        assertEquals("Alex", response.firstName());
        assertEquals("Smith", response.lastName());
        assertNull(response.email());
        assertNull(response.mobilePhone());
        assertEquals(InvitationStatus.SENT, invitation.getStatus());
        assertNull(response.submittedAt());
        assertEquals(ClientType.INDIVIDUAL, response.clientType());
    }

    @Test
    void putUpdatesExistingQuestionnaireDraftForSentInvitation() {
        OnboardingInvitation invitation = sentInvitation();
        Questionnaire questionnaire = new Questionnaire(invitation, partialRequest(), Instant.now());
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.of(questionnaire));

        QuestionnaireResponse response = service.createOrUpdateQuestionnaire(
                invitation.getToken().toString(),
                request(false)
        );

        assertEquals("alex@example.com", response.email());
        assertEquals("0400000000", response.mobilePhone());
        assertEquals("1 Main Street", response.addressLine1());
        assertEquals(InvitationStatus.SENT, invitation.getStatus());
        verify(questionnaireRepository).save(questionnaire);
    }

    @Test
    void putUsesClientTypeFromInvitation() {
        OnboardingInvitation invitation = sentInvitation(ClientType.COMPANY);
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.empty());

        QuestionnaireResponse response = service.createOrUpdateQuestionnaire(invitation.getToken().toString(), request(true));

        assertEquals(ClientType.COMPANY, response.clientType());
        assertNotNull(response.clientType());
    }

    @Test
    void putRejectedForDraftInvitation() {
        OnboardingInvitation invitation = draftInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(PublicOnboardingUnavailableException.class,
                () -> service.createOrUpdateQuestionnaire(invitation.getToken().toString(), request(false)));

        assertEquals("PUBLIC_ONBOARDING_UNAVAILABLE", exception.getCode());
        assertEquals("This invitation is not available.", exception.getMessage());
    }

    @Test
    void putRejectedForSubmittedInvitation() {
        OnboardingInvitation invitation = submittedInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(PublicOnboardingUnavailableException.class,
                () -> service.createOrUpdateQuestionnaire(invitation.getToken().toString(), partialRequest()));

        assertEquals("PUBLIC_ONBOARDING_ALREADY_SUBMITTED", exception.getCode());
    }

    @Test
    void putRejectedForExpiredSentInvitation() {
        OnboardingInvitation invitation = expiredSentInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(PublicOnboardingUnavailableException.class,
                () -> service.createOrUpdateQuestionnaire(invitation.getToken().toString(), request(false)));

        assertEquals("PUBLIC_ONBOARDING_EXPIRED", exception.getCode());
        assertEquals(
                "This invitation has expired. Please contact the accounting team if you need a new invitation.",
                exception.getMessage()
        );
    }

    @Test
    void putRejectedForCancelledInvitation() {
        OnboardingInvitation invitation = draftInvitation();
        invitation.markCancelled(Instant.now());
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(PublicOnboardingUnavailableException.class,
                () -> service.createOrUpdateQuestionnaire(invitation.getToken().toString(), partialRequest()));

        assertEquals("PUBLIC_ONBOARDING_CANCELLED", exception.getCode());
    }

    @Test
    void putRejectedForSentInvitationWithoutExpirationTimestamp() {
        // Arrange
        OnboardingInvitation invitation = sentInvitation();
        ReflectionTestUtils.setField(invitation, "expiresAt", null);

        when(invitationRepository.findByToken(invitation.getToken()))
                .thenReturn(Optional.of(invitation));

        // Act + Assert
        PublicOnboardingUnavailableException exception = assertThrows(
                PublicOnboardingUnavailableException.class,
                () -> service.createOrUpdateQuestionnaire(
                        invitation.getToken().toString(),
                        request(false)
                )
        );

        assertEquals("PUBLIC_ONBOARDING_EXPIRED", exception.getCode());
    }

    @Test
    void getSubmittedInvitationReturnsAlreadySubmittedResponse() {
        OnboardingInvitation invitation = submittedInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(
                PublicOnboardingUnavailableException.class,
                () -> service.getOnboarding(invitation.getToken().toString())
        );

        assertEquals("PUBLIC_ONBOARDING_ALREADY_SUBMITTED", exception.getCode());
        assertEquals("This questionnaire has already been submitted. Thank you.", exception.getMessage());
    }

    @Test
    void getApprovedInvitationReturnsAlreadySubmittedResponse() {
        OnboardingInvitation invitation = submittedInvitation();
        invitation.markApproved(Instant.now());
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(
                PublicOnboardingUnavailableException.class,
                () -> service.getOnboarding(invitation.getToken().toString())
        );

        assertEquals("PUBLIC_ONBOARDING_ALREADY_SUBMITTED", exception.getCode());
        assertEquals("This questionnaire has already been submitted. Thank you.", exception.getMessage());
    }

    @Test
    void getXpmSentInvitationReturnsAlreadySubmittedResponse() {
        OnboardingInvitation invitation = submittedInvitation();
        ReflectionTestUtils.setField(invitation, "status", InvitationStatus.XPM_SENT);
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(
                PublicOnboardingUnavailableException.class,
                () -> service.getOnboarding(invitation.getToken().toString())
        );

        assertEquals("PUBLIC_ONBOARDING_ALREADY_SUBMITTED", exception.getCode());
        assertEquals("This questionnaire has already been submitted. Thank you.", exception.getMessage());
    }

    @Test
    void getExpiredInvitationReturnsExpiredResponse() {
        OnboardingInvitation invitation = expiredInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(
                PublicOnboardingUnavailableException.class,
                () -> service.getOnboarding(invitation.getToken().toString())
        );

        assertEquals("PUBLIC_ONBOARDING_EXPIRED", exception.getCode());
        assertEquals(
                "This invitation has expired. Please contact the accounting team if you need a new invitation.",
                exception.getMessage()
        );
    }

    @Test
    void getCancelledInvitationReturnsInactiveResponse() {
        OnboardingInvitation invitation = draftInvitation();
        invitation.markCancelled(Instant.now());
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(
                PublicOnboardingUnavailableException.class,
                () -> service.getOnboarding(invitation.getToken().toString())
        );

        assertEquals("PUBLIC_ONBOARDING_CANCELLED", exception.getCode());
        assertEquals(
                "This invitation is no longer active. Please contact the accounting team if you have any questions.",
                exception.getMessage()
        );
    }

    @Test
    void getUnexpectedUnavailableInvitationReturnsGenericResponse() {
        OnboardingInvitation invitation = draftInvitation();
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        PublicOnboardingUnavailableException exception = assertThrows(
                PublicOnboardingUnavailableException.class,
                () -> service.getOnboarding(invitation.getToken().toString())
        );

        assertEquals("PUBLIC_ONBOARDING_UNAVAILABLE", exception.getCode());
        assertEquals("This invitation is not available.", exception.getMessage());
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
        assertEquals(ClientType.INDIVIDUAL, response.clientType());
        assertNotNull(response.clientType());
    }

    @Test
    void postSubmitUsesSavedDraftDataAndChangesStatusToSubmitted() {
        OnboardingInvitation invitation = sentInvitation();
        Questionnaire questionnaire = new Questionnaire(invitation, request(true), Instant.now());
        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(questionnaireRepository.findByInvitationId(invitation.getId())).thenReturn(Optional.of(questionnaire));

        QuestionnaireResponse response = service.submitQuestionnaire(invitation.getToken().toString());

        assertEquals(InvitationStatus.SUBMITTED, invitation.getStatus());
        assertEquals("Alex", response.firstName());
        assertEquals("Smith", response.lastName());
        assertEquals("alex@example.com", response.email());
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
        return draftInvitation(ClientType.INDIVIDUAL);
    }

    private OnboardingInvitation draftInvitation(ClientType clientType) {
        return new OnboardingInvitation(
                UUID.randomUUID(),
                "Alex Smith",
                "alex@example.com",
                clientType,
                Instant.now(),
                null
        );
    }

    private OnboardingInvitation sentInvitation() {
        return sentInvitation(ClientType.INDIVIDUAL);
    }

    private OnboardingInvitation sentInvitation(ClientType clientType) {
        OnboardingInvitation invitation = draftInvitation(clientType);
        invitation.markSent(Instant.now(), INVITATION_VALIDITY);
        return invitation;
    }

    private OnboardingInvitation submittedInvitation() {
        OnboardingInvitation invitation = sentInvitation();
        invitation.markSubmitted(Instant.now());
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

    private OnboardingInvitation expiredInvitation() {
        OnboardingInvitation invitation = expiredSentInvitation();
        invitation.markExpired(Instant.now());
        return invitation;
    }

    private PublicQuestionnaireRequest request(boolean clientConfirmed) {
        return new PublicQuestionnaireRequest(
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

    private PublicQuestionnaireRequest partialRequest() {
        return new PublicQuestionnaireRequest(
                "Alex",
                null,
                "Smith",
                LocalDate.of(1990, 1, 15),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Australia",
                false
        );
    }
}
