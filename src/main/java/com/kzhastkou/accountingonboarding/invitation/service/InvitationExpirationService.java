package com.kzhastkou.accountingonboarding.invitation.service;

import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.repository.OnboardingInvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class InvitationExpirationService {

    private final OnboardingInvitationRepository invitationRepository;
    public InvitationExpirationService(OnboardingInvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public int expireInvitations() {
        return invitationRepository.expireInvitations(
                InvitationStatus.SENT,
                InvitationStatus.EXPIRED,
                Instant.now()
        );
    }
}
