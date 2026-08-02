package com.kzhastkou.accountingonboarding.invitation.service;

import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.service.AuditLogService;
import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.invitation.repository.OnboardingInvitationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationExpirationServiceTest {

    @Mock
    OnboardingInvitationRepository repository;

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    InvitationExpirationService service;

    @Test
    void expiresEligibleSentInvitationsAndRecordsAuditEvents() {
        OnboardingInvitation firstInvitation = expiredSentInvitation(1L);
        OnboardingInvitation secondInvitation = expiredSentInvitation(2L);
        when(repository.findAllByStatusAndExpiresAtLessThanEqual(
                eq(InvitationStatus.SENT),
                any(Instant.class)
        )).thenReturn(List.of(firstInvitation, secondInvitation));

        int result = service.expireInvitations();

        assertEquals(2, result);
        assertEquals(InvitationStatus.EXPIRED, firstInvitation.getStatus());
        assertEquals(InvitationStatus.EXPIRED, secondInvitation.getStatus());
        verify(repository).findAllByStatusAndExpiresAtLessThanEqual(
                eq(InvitationStatus.SENT),
                any(Instant.class)
        );
        verify(auditLogService).recordSystemSuccess(
                AuditAction.INVITATION_EXPIRED,
                "INVITATION",
                1L,
                "Invitation expired"
        );
        verify(auditLogService).recordSystemSuccess(
                AuditAction.INVITATION_EXPIRED,
                "INVITATION",
                2L,
                "Invitation expired"
        );
    }

    @Test
    void doesNotRecordAuditEventsWhenNoInvitationsAreEligible() {
        when(repository.findAllByStatusAndExpiresAtLessThanEqual(
                eq(InvitationStatus.SENT),
                any(Instant.class)
        )).thenReturn(List.of());

        int result = service.expireInvitations();

        assertEquals(0, result);
        verifyNoInteractions(auditLogService);
    }

    private OnboardingInvitation expiredSentInvitation(Long id) {
        Instant sentAt = Instant.now().minus(Duration.ofDays(11));
        OnboardingInvitation invitation = new OnboardingInvitation(
                UUID.randomUUID(),
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL,
                sentAt,
                null
        );
        invitation.markSent(sentAt, Duration.ofDays(10));
        ReflectionTestUtils.setField(invitation, "id", id);
        return invitation;
    }
}
