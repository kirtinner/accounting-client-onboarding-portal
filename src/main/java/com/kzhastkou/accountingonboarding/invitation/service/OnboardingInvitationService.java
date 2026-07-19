package com.kzhastkou.accountingonboarding.invitation.service;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.exception.NotFoundException;
import com.kzhastkou.accountingonboarding.email.InvitationEmailService;
import com.kzhastkou.accountingonboarding.invitation.dto.CreateOnboardingInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.dto.OnboardingInvitationResponse;
import com.kzhastkou.accountingonboarding.invitation.dto.UpdateOnboardingInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.invitation.repository.OnboardingInvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OnboardingInvitationService {

    private static final Duration INVITATION_VALIDITY = Duration.ofDays(10);

    private final OnboardingInvitationRepository repository;
    private final InvitationEmailService invitationEmailService;

    public OnboardingInvitationService(OnboardingInvitationRepository repository,
                                       InvitationEmailService invitationEmailService) {
        this.repository = repository;
        this.invitationEmailService = invitationEmailService;
    }

    @Transactional
    public OnboardingInvitationResponse createInvitation(CreateOnboardingInvitationRequest request) {
        Instant createdAt = Instant.now();
        OnboardingInvitation invitation = new OnboardingInvitation(
                generateToken(),
                request.preferredName(),
                request.email(),
                request.clientType(),
                InvitationStatus.DRAFT,
                createdAt,
                createdAt.plus(INVITATION_VALIDITY),
                null
        );

        return toResponse(repository.save(invitation));
    }

    //    @Transactional(readOnly = true)
//    public List<OnboardingInvitationResponse> getInvitations() {
//        return repository.findAllByOrderByCreatedAtDesc().stream()
//                .map(this::toResponse)
//                .toList();
//    }
    @Transactional(readOnly = true)
    public List<OnboardingInvitationResponse> getInvitations(List<InvitationStatus> statuses) {
        List<OnboardingInvitation> onboardingInvitations;
        if (statuses == null || statuses.isEmpty()) {
            onboardingInvitations = repository.findAllByOrderByCreatedAtDesc();
        } else {
            onboardingInvitations = repository.findAllByStatusInOrderByCreatedAtDesc(statuses);
        }

        return onboardingInvitations.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OnboardingInvitationResponse getInvitation(Long id) {
        return toResponse(findInvitation(id));
    }

    @Transactional
    public OnboardingInvitationResponse updateInvitation(Long id, UpdateOnboardingInvitationRequest request) {
        OnboardingInvitation invitation = findInvitation(id);
        if (invitation.getStatus() != InvitationStatus.DRAFT) {
            throw new BadRequestException("Invitation can only be updated from DRAFT status");
        }

        invitation.updateDetails(request.preferredName(), request.email(), request.clientType());
        return toResponse(invitation);
    }

    @Transactional
    public OnboardingInvitationResponse sendInvitation(Long id) {
        OnboardingInvitation invitation = findInvitation(id);
        if (invitation.getStatus() != InvitationStatus.DRAFT) {
            throw new BadRequestException("Invitation can only be sent from DRAFT status");
        }

        invitation.markSent(Instant.now());
        invitationEmailService.sendInvitation(invitation);
        return toResponse(invitation);
    }

    @Transactional
    public OnboardingInvitationResponse cancelInvitation(Long id) {
        OnboardingInvitation invitation = findInvitation(id);
        if (invitation.getStatus() != InvitationStatus.DRAFT && invitation.getStatus() != InvitationStatus.SENT) {
            throw new BadRequestException("Invitation can only be cancelled from DRAFT or SENT status");
        }

        invitation.markCancelled(Instant.now());
        return toResponse(invitation);
    }

    private OnboardingInvitation findInvitation(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Onboarding invitation not found"));
    }

    private UUID generateToken() {
        UUID token;
        do {
            token = UUID.randomUUID();
        } while (repository.existsByToken(token));
        return token;
    }

    private OnboardingInvitationResponse toResponse(OnboardingInvitation invitation) {
        return new OnboardingInvitationResponse(
                invitation.getId(),
                invitation.getToken(),
                invitation.getPreferredName(),
                invitation.getEmail(),
                invitation.getClientType(),
                invitation.getStatus(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt(),
                invitation.getSentAt(),
                invitation.getSubmittedAt(),
                invitation.getApprovedAt(),
                invitation.getXpmSentAt(),
                invitation.getCancelledAt(),
                invitation.getCreatedBy()
        );
    }
}
