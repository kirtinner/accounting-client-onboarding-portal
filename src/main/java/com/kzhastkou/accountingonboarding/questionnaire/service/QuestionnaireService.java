package com.kzhastkou.accountingonboarding.questionnaire.service;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.exception.NotFoundException;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireRequest;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireResponse;
import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import com.kzhastkou.accountingonboarding.questionnaire.repository.QuestionnaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class QuestionnaireService {

    private final QuestionnaireRepository repository;

    public QuestionnaireService(QuestionnaireRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public QuestionnaireResponse getByInvitationId(Long invitationId) {
        return repository.findByInvitationId(invitationId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Questionnaire not found"));
    }

    @Transactional
    public QuestionnaireResponse createOrUpdateForInvitation(OnboardingInvitation invitation, QuestionnaireRequest request) {
        requireWritableInvitation(invitation, "Questionnaire can only be created or updated for SENT invitations");

        Instant now = Instant.now();
        Questionnaire questionnaire = repository.findByInvitationId(invitation.getId())
                .map(existing -> {
                    existing.updateFrom(request, now);
                    return existing;
                })
                .orElseGet(() -> new Questionnaire(invitation, request, now));

        return toResponse(repository.save(questionnaire));
    }

    @Transactional
    public QuestionnaireResponse submitForInvitation(OnboardingInvitation invitation) {
        requireWritableInvitation(invitation, "Questionnaire can only be submitted for SENT invitations");

        Questionnaire questionnaire = repository.findByInvitationId(invitation.getId())
                .orElseThrow(() -> new BadRequestException("Questionnaire must exist before submission"));
        if (!questionnaire.isClientConfirmed()) {
            throw new BadRequestException("Client confirmation is required before submitting questionnaire");
        }

        Instant submittedAt = Instant.now();
        questionnaire.submit(submittedAt);
        invitation.markSubmitted(submittedAt);
        return toResponse(questionnaire);
    }

    private void requireWritableInvitation(OnboardingInvitation invitation, String message) {
        if (invitation.getStatus() != InvitationStatus.SENT) {
            throw new BadRequestException(message);
        }
        if (!invitation.getExpiresAt().isAfter(Instant.now())) {
            throw new BadRequestException("Invitation is expired");
        }
    }

    private QuestionnaireResponse toResponse(Questionnaire questionnaire) {
        return new QuestionnaireResponse(
                questionnaire.getId(),
                questionnaire.getInvitation().getId(),
                questionnaire.getClientType(),
                questionnaire.getFirstName(),
                questionnaire.getMiddleName(),
                questionnaire.getLastName(),
                questionnaire.getDateOfBirth(),
                questionnaire.getEmail(),
                questionnaire.getMobilePhone(),
                questionnaire.getAddressLine1(),
                questionnaire.getAddressLine2(),
                questionnaire.getSuburb(),
                questionnaire.getState(),
                questionnaire.getPostcode(),
                questionnaire.getCountry(),
                questionnaire.isClientConfirmed(),
                questionnaire.getSubmittedAt(),
                questionnaire.getCreatedAt(),
                questionnaire.getUpdatedAt()
        );
    }
}
