package com.kzhastkou.accountingonboarding.questionnaire.service;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.questionnaire.dto.PublicQuestionnaireRequest;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionnaireServiceTest {

    private static final Duration INVITATION_VALIDITY = Duration.ofDays(10);

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
        assertEquals(ClientType.INDIVIDUAL, response.clientType());
        assertNotNull(response.clientType());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
    }

    @Test
    void createIncompleteDraftForSentInvitationSucceeds() {
        OnboardingInvitation invitation = sentInvitation();
        when(repository.findByInvitationId(invitation.getId())).thenReturn(Optional.empty());

        QuestionnaireResponse response = service.createOrUpdateForInvitation(invitation, partialRequest());

        assertEquals("Alex", response.firstName());
        assertEquals("Smith", response.lastName());
        assertNull(response.email());
        assertNull(response.mobilePhone());
        assertNull(response.addressLine1());
        assertEquals(InvitationStatus.SENT, invitation.getStatus());
        assertNull(response.submittedAt());
        assertEquals(ClientType.INDIVIDUAL, response.clientType());
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
        assertEquals(ClientType.INDIVIDUAL, response.clientType());
        assertNotNull(response.clientType());
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

    @Test
    void submitIncompleteQuestionnaireFailsWithoutChangingInvitationStatus() {
        OnboardingInvitation invitation = sentInvitation();
        Questionnaire questionnaire = new Questionnaire(invitation, partialRequest(), Instant.now());
        when(repository.findByInvitationId(invitation.getId())).thenReturn(Optional.of(questionnaire));

        assertThrows(BadRequestException.class, () -> service.submitForInvitation(invitation));
        assertEquals(InvitationStatus.SENT, invitation.getStatus());
        assertNull(questionnaire.getSubmittedAt());
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

    private PublicQuestionnaireRequest updatedRequest(boolean clientConfirmed) {
        return new PublicQuestionnaireRequest(
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

    @Test
    void createOrUpdateForSentInvitationWithoutExpirationTimestampFails() {
        // Arrange
        OnboardingInvitation invitation = sentInvitation();
        ReflectionTestUtils.setField(invitation, "expiresAt", null);

        // Act + Assert
        assertThrows(
                BadRequestException.class,
                () -> service.createOrUpdateForInvitation(
                        invitation,
                        request(false)
                )
        );
    }
}
