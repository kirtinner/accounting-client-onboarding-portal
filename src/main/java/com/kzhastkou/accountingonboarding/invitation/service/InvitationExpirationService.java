package com.kzhastkou.accountingonboarding.invitation.service;

import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.service.AuditLogService;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.invitation.repository.OnboardingInvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class InvitationExpirationService {

    private static final String INVITATION_ENTITY_TYPE = "INVITATION";

    private final OnboardingInvitationRepository invitationRepository;
    private final AuditLogService auditLogService;

    public InvitationExpirationService(OnboardingInvitationRepository invitationRepository,
                                       AuditLogService auditLogService) {
        this.invitationRepository = invitationRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public int expireInvitations() {
        Instant now = Instant.now();
        List<OnboardingInvitation> expiredInvitations =
                invitationRepository.findAllByStatusAndExpiresAtLessThanEqual(InvitationStatus.SENT, now);

        expiredInvitations.forEach(invitation -> {
            invitation.markExpired(now);
            auditLogService.recordSystemSuccess(
                    AuditAction.INVITATION_EXPIRED,
                    INVITATION_ENTITY_TYPE,
                    invitation.getId(),
                    "Invitation expired"
            );
        });

        return expiredInvitations.size();
    }
}
