package com.kzhastkou.accountingonboarding.invitation.service;

import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.service.AuditLogService;
import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.email.InvitationEmailService;
import com.kzhastkou.accountingonboarding.invitation.dto.CreateOnboardingInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.dto.OnboardingInvitationResponse;
import com.kzhastkou.accountingonboarding.invitation.dto.UpdateOnboardingInvitationRequest;
import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.invitation.repository.OnboardingInvitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OnboardingInvitationServiceTest {

    private static final Duration INVITATION_VALIDITY = Duration.ofDays(10);

    private OnboardingInvitationRepository repository;
    private InvitationEmailService invitationEmailService;
    private AuditLogService auditLogService;
    private OnboardingInvitationService service;

    @BeforeEach
    void setUp() {
        repository = mock(OnboardingInvitationRepository.class);
        invitationEmailService = mock(InvitationEmailService.class);
        auditLogService = mock(AuditLogService.class);
        service = new OnboardingInvitationService(repository, invitationEmailService, auditLogService);

        when(repository.existsByToken(any(UUID.class))).thenReturn(false);
        when(repository.save(any(OnboardingInvitation.class))).thenAnswer(invocation -> {
            OnboardingInvitation invitation = invocation.getArgument(0);
            ReflectionTestUtils.setField(invitation, "id", 1L);
            return invitation;
        });
    }

    @Test
    void createInvitationCreatesDraftInvitationWithoutExpiry() {
        CreateOnboardingInvitationRequest request =
                new CreateOnboardingInvitationRequest(
                        "Alex Smith",
                        "alex@example.com",
                        ClientType.INDIVIDUAL
                );

        OnboardingInvitationResponse response =
                service.createInvitation(request);

        assertNotNull(response.token());
        assertEquals(InvitationStatus.DRAFT, response.status());
        assertNotNull(response.createdAt());
        assertNull(response.sentAt());
        assertNull(response.expiresAt());
        verify(auditLogService).recordSystemSuccess(
                AuditAction.INVITATION_CREATED,
                "INVITATION",
                1L,
                "Invitation created"
        );
    }

    @Test
    void sendChangesDraftInvitationToSent() {
        OnboardingInvitation invitation = draftInvitation();
        when(repository.findById(1L)).thenReturn(Optional.of(invitation));

        OnboardingInvitationResponse response = service.sendInvitation(1L);

        assertEquals(InvitationStatus.SENT, response.status());
        assertNotNull(response.sentAt());
        assertNotNull(response.expiresAt());
        assertEquals(
                response.sentAt().plus(INVITATION_VALIDITY),
                response.expiresAt()
        );
        verify(invitationEmailService).sendInvitation(invitation);
        verify(auditLogService).recordSystemSuccess(
                AuditAction.INVITATION_SENT,
                "INVITATION",
                1L,
                "Invitation sent"
        );
    }

    @Test
    void markSentRejectsNullSentAt() {
        OnboardingInvitation invitation = draftInvitation();

        assertThrows(
                IllegalArgumentException.class,
                () -> invitation.markSent(null, INVITATION_VALIDITY)
        );
        assertEquals(InvitationStatus.DRAFT, invitation.getStatus());
        assertNull(invitation.getSentAt());
        assertNull(invitation.getExpiresAt());
    }

    @Test
    void markSentRejectsNullValidity() {
        OnboardingInvitation invitation = draftInvitation();

        assertThrows(
                IllegalArgumentException.class,
                () -> invitation.markSent(Instant.now(), null)
        );
        assertEquals(InvitationStatus.DRAFT, invitation.getStatus());
        assertNull(invitation.getSentAt());
        assertNull(invitation.getExpiresAt());
    }

    @Test
    void markSentRejectsZeroValidity() {
        OnboardingInvitation invitation = draftInvitation();

        assertThrows(
                IllegalArgumentException.class,
                () -> invitation.markSent(Instant.now(), Duration.ZERO)
        );
        assertEquals(InvitationStatus.DRAFT, invitation.getStatus());
        assertNull(invitation.getSentAt());
        assertNull(invitation.getExpiresAt());
    }

    @Test
    void markSentRejectsNegativeValidity() {
        OnboardingInvitation invitation = draftInvitation();

        assertThrows(
                IllegalArgumentException.class,
                () -> invitation.markSent(Instant.now(), Duration.ofDays(-1))
        );
        assertEquals(InvitationStatus.DRAFT, invitation.getStatus());
        assertNull(invitation.getSentAt());
        assertNull(invitation.getExpiresAt());
    }

    @Test
    void markSentRejectsInvalidStatus() {
        OnboardingInvitation invitation = draftInvitation();
        invitation.markCancelled(Instant.now());

        assertThrows(
                BadRequestException.class,
                () -> invitation.markSent(Instant.now(), INVITATION_VALIDITY)
        );
        assertEquals(InvitationStatus.CANCELLED, invitation.getStatus());
        assertNull(invitation.getSentAt());
        assertNull(invitation.getExpiresAt());
    }

    @Test
    void cancelChangesSentInvitationToCancelled() {
        OnboardingInvitation invitation = draftInvitation();
        when(repository.findById(1L)).thenReturn(Optional.of(invitation));

        service.sendInvitation(1L);
        OnboardingInvitationResponse response = service.cancelInvitation(1L);

        assertEquals(InvitationStatus.CANCELLED, response.status());
        assertNotNull(response.cancelledAt());
        verify(auditLogService).recordSystemSuccess(
                AuditAction.INVITATION_SENT,
                "INVITATION",
                1L,
                "Invitation sent"
        );
        verify(auditLogService).recordSystemSuccess(
                AuditAction.INVITATION_CANCELLED,
                "INVITATION",
                1L,
                "Invitation cancelled"
        );
    }

    @Test
    void invalidSendFromCancelledFails() {
        OnboardingInvitation invitation = draftInvitation();
        when(repository.findById(1L)).thenReturn(Optional.of(invitation));

        service.cancelInvitation(1L);
        clearInvocations(auditLogService);

        assertThrows(BadRequestException.class, () -> service.sendInvitation(1L));
        verifyNoInteractions(auditLogService);
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
        clearInvocations(auditLogService);

        assertThrows(BadRequestException.class, () -> service.updateInvitation(1L, new UpdateOnboardingInvitationRequest(
                "Taylor Brown",
                "taylor@example.com",
                ClientType.COMPANY
        )));
        verifyNoInteractions(auditLogService);
    }

    private OnboardingInvitation draftInvitation() {
        OnboardingInvitation invitation = new OnboardingInvitation(
                UUID.randomUUID(),
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL,
                Instant.now(),
                null
        );
        ReflectionTestUtils.setField(invitation, "id", 1L);
        return invitation;
    }
}
