package com.kzhastkou.accountingonboarding.invitation.service;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.email.InvitationEmailService;
import com.kzhastkou.accountingonboarding.invitation.dto.CreateOnboardingInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.dto.OnboardingInvitationResponse;
import com.kzhastkou.accountingonboarding.invitation.dto.UpdateOnboardingInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.entity.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.invitation.repository.OnboardingInvitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnboardingInvitationServiceTest {

    private OnboardingInvitationRepository repository;
    private InvitationEmailService invitationEmailService;
    private OnboardingInvitationService service;

    @BeforeEach
    void setUp() {
        repository = mock(OnboardingInvitationRepository.class);
        invitationEmailService = mock(InvitationEmailService.class);
        service = new OnboardingInvitationService(repository, invitationEmailService);

        when(repository.existsByToken(any(UUID.class))).thenReturn(false);
        when(repository.save(any(OnboardingInvitation.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createInvitationCreatesDraftInvitationWithTokenAndExpiry() {
        CreateOnboardingInvitationRequest request = new CreateOnboardingInvitationRequest(
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL
        );

        OnboardingInvitationResponse response = service.createInvitation(request);

        assertNotNull(response.token());
        assertEquals("Alex Smith", response.preferredName());
        assertEquals("alex@example.com", response.email());
        assertEquals(ClientType.INDIVIDUAL, response.clientType());
        assertEquals(InvitationStatus.DRAFT, response.status());
        assertNotNull(response.createdAt());
        assertEquals(Duration.ofDays(10), Duration.between(response.createdAt(), response.expiresAt()));
    }

    @Test
    void sendChangesDraftInvitationToSent() {
        OnboardingInvitation invitation = draftInvitation();
        when(repository.findById(1L)).thenReturn(Optional.of(invitation));

        OnboardingInvitationResponse response = service.sendInvitation(1L);

        assertEquals(InvitationStatus.SENT, response.status());
        assertNotNull(response.sentAt());
        verify(invitationEmailService).sendInvitation(invitation);
    }

    @Test
    void cancelChangesSentInvitationToCancelled() {
        OnboardingInvitation invitation = draftInvitation();
        when(repository.findById(1L)).thenReturn(Optional.of(invitation));

        service.sendInvitation(1L);
        OnboardingInvitationResponse response = service.cancelInvitation(1L);

        assertEquals(InvitationStatus.CANCELLED, response.status());
        assertNotNull(response.cancelledAt());
    }

    @Test
    void invalidSendFromCancelledFails() {
        OnboardingInvitation invitation = draftInvitation();
        when(repository.findById(1L)).thenReturn(Optional.of(invitation));

        service.cancelInvitation(1L);

        assertThrows(BadRequestException.class, () -> service.sendInvitation(1L));
    }

    @Test
    void updateDraftInvitationChangesEditableDetailsOnly() {
        OnboardingInvitation invitation = draftInvitation();
        when(repository.findById(1L)).thenReturn(Optional.of(invitation));

        OnboardingInvitationResponse original = service.getInvitation(1L);
        OnboardingInvitationResponse response = service.updateInvitation(1L, new UpdateOnboardingInvitationRequest(
                "Taylor Brown",
                "taylor@example.com",
                ClientType.COMPANY
        ));

        assertEquals("Taylor Brown", response.preferredName());
        assertEquals("taylor@example.com", response.email());
        assertEquals(ClientType.COMPANY, response.clientType());
        assertEquals(original.token(), response.token());
        assertEquals(original.status(), response.status());
        assertEquals(original.createdAt(), response.createdAt());
        assertEquals(original.expiresAt(), response.expiresAt());
    }

    @Test
    void updateSentInvitationFails() {
        OnboardingInvitation invitation = draftInvitation();
        when(repository.findById(1L)).thenReturn(Optional.of(invitation));

        service.sendInvitation(1L);

        assertThrows(BadRequestException.class, () -> service.updateInvitation(1L, new UpdateOnboardingInvitationRequest(
                "Taylor Brown",
                "taylor@example.com",
                ClientType.COMPANY
        )));
    }

    private OnboardingInvitation draftInvitation() {
        return new OnboardingInvitation(
                UUID.randomUUID(),
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL,
                InvitationStatus.DRAFT,
                java.time.Instant.now(),
                java.time.Instant.now().plus(Duration.ofDays(10)),
                null
        );
    }
}
