package com.kzhastkou.accountingonboarding.invitation.service;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.exception.NotFoundException;
import com.kzhastkou.accountingonboarding.invitation.dto.CreateInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.dto.InvitationResponse;
import com.kzhastkou.accountingonboarding.invitation.entity.Invitation;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.Practice;
import com.kzhastkou.accountingonboarding.invitation.repository.InvitationRepository;
import com.kzhastkou.accountingonboarding.invitation.repository.PracticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final PracticeRepository practiceRepository;

    public InvitationService(InvitationRepository invitationRepository, PracticeRepository practiceRepository) {
        this.invitationRepository = invitationRepository;
        this.practiceRepository = practiceRepository;
    }

    @Transactional
    public InvitationResponse createInvitation(CreateInvitationRequest request) {
        Practice practice = null;
        if (request.practiceId() != null) {
            practice = practiceRepository.findById(request.practiceId())
                    .orElseThrow(() -> new NotFoundException("Practice not found"));
        }

        Invitation invitation = new Invitation(
                UUID.randomUUID().toString(),
                request.clientEmail(),
                InvitationStatus.CREATED,
                request.expiresAt(),
                LocalDateTime.now(),
                practice
        );

        return toResponse(invitationRepository.save(invitation));
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getInvitations() {
        return invitationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InvitationResponse getPublicInvitation(String token) {
        Invitation invitation = getInvitationByToken(token);
        expireIfNeeded(invitation);
        return toResponse(invitation);
    }

    @Transactional
    public Invitation getOpenInvitationByToken(String token) {
        Invitation invitation = getInvitationByToken(token);
        expireIfNeeded(invitation);

        if (invitation.getStatus() != InvitationStatus.CREATED) {
            throw new BadRequestException("Invitation is not open for submission");
        }

        return invitation;
    }

    private Invitation getInvitationByToken(String token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invitation not found"));
    }

    private void expireIfNeeded(Invitation invitation) {
        if (invitation.getStatus() == InvitationStatus.CREATED && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
        }
    }

    public InvitationResponse toResponse(Invitation invitation) {
        Long practiceId = invitation.getPractice() == null ? null : invitation.getPractice().getId();
        return new InvitationResponse(
                invitation.getId(),
                invitation.getToken(),
                invitation.getClientEmail(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt(),
                practiceId
        );
    }
}
