package com.kzhastkou.accountingonboarding.questionnaire.service;

import com.kzhastkou.accountingonboarding.common.exception.NotFoundException;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.invitation.repository.OnboardingInvitationRepository;
import com.kzhastkou.accountingonboarding.questionnaire.dto.PublicOnboardingResponse;
import com.kzhastkou.accountingonboarding.questionnaire.dto.PublicQuestionnaireRequest;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireResponse;
import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import com.kzhastkou.accountingonboarding.questionnaire.exception.PublicOnboardingUnavailableException;
import com.kzhastkou.accountingonboarding.questionnaire.repository.QuestionnaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PublicQuestionnaireService {

    private static final String ALREADY_SUBMITTED_CODE = "PUBLIC_ONBOARDING_ALREADY_SUBMITTED";
    private static final String EXPIRED_CODE = "PUBLIC_ONBOARDING_EXPIRED";
    private static final String CANCELLED_CODE = "PUBLIC_ONBOARDING_CANCELLED";
    private static final String UNAVAILABLE_CODE = "PUBLIC_ONBOARDING_UNAVAILABLE";
    private static final String ALREADY_SUBMITTED_MESSAGE =
            "This questionnaire has already been submitted. Thank you.";
    private static final String EXPIRED_MESSAGE =
            "This invitation has expired. Please contact the accounting team if you need a new invitation.";
    private static final String CANCELLED_MESSAGE =
            "This invitation is no longer active. Please contact the accounting team if you have any questions.";
    private static final String UNAVAILABLE_MESSAGE = "This invitation is not available.";

    private final OnboardingInvitationRepository invitationRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionnaireService questionnaireService;

    public PublicQuestionnaireService(OnboardingInvitationRepository invitationRepository,
                                      QuestionnaireRepository questionnaireRepository,
                                      QuestionnaireService questionnaireService) {
        this.invitationRepository = invitationRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.questionnaireService = questionnaireService;
    }

    @Transactional(readOnly = true)
    public PublicOnboardingResponse getOnboarding(String token) {
        OnboardingInvitation invitation = findUsableInvitation(token);
        QuestionnaireResponse questionnaire = questionnaireRepository.findByInvitationId(invitation.getId())
                .map(this::toResponse)
                .orElse(null);
        return toPublicResponse(invitation, questionnaire);
    }

    @Transactional
    public QuestionnaireResponse createOrUpdateQuestionnaire(String token, PublicQuestionnaireRequest request) {
        OnboardingInvitation invitation = findUsableInvitation(token);
        return questionnaireService.createOrUpdateForInvitation(invitation, request);
    }

    @Transactional
    public QuestionnaireResponse submitQuestionnaire(String token) {
        OnboardingInvitation invitation = findUsableInvitation(token);
        return questionnaireService.submitForInvitation(invitation);
    }

    private OnboardingInvitation findUsableInvitation(String token) {
        OnboardingInvitation invitation = invitationRepository.findByToken(parseToken(token))
                .orElseThrow(() -> new NotFoundException("Onboarding invitation not found"));
        validatePublicAccess(invitation);
        return invitation;
    }

    private UUID parseToken(String token) {
        try {
            return UUID.fromString(token);
        } catch (IllegalArgumentException exception) {
            throw new NotFoundException("Onboarding invitation not found");
        }
    }

    private void validatePublicAccess(OnboardingInvitation invitation) {
        InvitationStatus status = invitation.getStatus();
        if (status == InvitationStatus.SUBMITTED
                || status == InvitationStatus.APPROVED
                || status == InvitationStatus.XPM_SENT) {
            throw new PublicOnboardingUnavailableException(ALREADY_SUBMITTED_CODE, ALREADY_SUBMITTED_MESSAGE);
        }

        if (status == InvitationStatus.EXPIRED) {
            throw new PublicOnboardingUnavailableException(EXPIRED_CODE, EXPIRED_MESSAGE);
        }

        if (status == InvitationStatus.CANCELLED) {
            throw new PublicOnboardingUnavailableException(CANCELLED_CODE, CANCELLED_MESSAGE);
        }

        if (status != InvitationStatus.SENT) {
            throw new PublicOnboardingUnavailableException(UNAVAILABLE_CODE, UNAVAILABLE_MESSAGE);
        }

        Instant expiresAt = invitation.getExpiresAt();

        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            throw new PublicOnboardingUnavailableException(EXPIRED_CODE, EXPIRED_MESSAGE);
        }
    }

    private PublicOnboardingResponse toPublicResponse(OnboardingInvitation invitation,
                                                     QuestionnaireResponse questionnaire) {
        return new PublicOnboardingResponse(
                invitation.getId(),
                invitation.getToken(),
                invitation.getPreferredName(),
                invitation.getEmail(),
                invitation.getClientType(),
                invitation.getExpiresAt(),
                questionnaire
        );
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
