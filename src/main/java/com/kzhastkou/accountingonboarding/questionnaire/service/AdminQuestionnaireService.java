package com.kzhastkou.accountingonboarding.questionnaire.service;

import com.kzhastkou.accountingonboarding.audit.entity.AuditAction;
import com.kzhastkou.accountingonboarding.audit.service.AuditLogService;
import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.exception.NotImplementedException;
import com.kzhastkou.accountingonboarding.common.exception.NotFoundException;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.questionnaire.dto.AdminQuestionnaireDetailResponse;
import com.kzhastkou.accountingonboarding.questionnaire.dto.AdminQuestionnaireSummaryResponse;
import com.kzhastkou.accountingonboarding.questionnaire.dto.AdminQuestionnaireUpdateRequest;
import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import com.kzhastkou.accountingonboarding.questionnaire.repository.QuestionnaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;

@Service
public class AdminQuestionnaireService {

    private static final String QUESTIONNAIRE_ENTITY_TYPE = "QUESTIONNAIRE";

    private final QuestionnaireRepository repository;
    private final AuditLogService auditLogService;

    public AdminQuestionnaireService(QuestionnaireRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<AdminQuestionnaireSummaryResponse> getQuestionnaires(List<InvitationStatus> invitationStatuses) {
        List<InvitationStatus> effectiveStatuses =
                CollectionUtils.isEmpty(invitationStatuses)
                        ? List.of(InvitationStatus.SUBMITTED, InvitationStatus.APPROVED, InvitationStatus.XPM_SENT)
                        : invitationStatuses;

        return repository.findAllForAdminReviewByInvitationStatuses(effectiveStatuses)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminQuestionnaireDetailResponse getQuestionnaire(Long id) {
        return repository.findByIdWithInvitation(id)
                .map(this::toDetailResponse)
                .orElseThrow(() -> new NotFoundException("Questionnaire not found"));
    }

    @Transactional
    public AdminQuestionnaireDetailResponse updateQuestionnaire(Long id, AdminQuestionnaireUpdateRequest request) {
        Questionnaire questionnaire = findQuestionnaire(id);
        questionnaire.updateFrom(request, Instant.now());
        auditLogService.recordSystemSuccess(
                AuditAction.QUESTIONNAIRE_UPDATED,
                QUESTIONNAIRE_ENTITY_TYPE,
                questionnaire.getId(),
                "Questionnaire updated"
        );
        return toDetailResponse(questionnaire);
    }

    @Transactional
    public AdminQuestionnaireDetailResponse approveQuestionnaire(Long id) {
        Questionnaire questionnaire = findQuestionnaire(id);
        questionnaire.getInvitation().markApproved(Instant.now());
        auditLogService.recordSystemSuccess(
                AuditAction.QUESTIONNAIRE_APPROVED,
                QUESTIONNAIRE_ENTITY_TYPE,
                questionnaire.getId(),
                "Questionnaire approved"
        );
        return toDetailResponse(questionnaire);
    }

    @Transactional
    public AdminQuestionnaireDetailResponse reopenQuestionnaire(Long id) {
        Questionnaire questionnaire = findQuestionnaire(id);
        questionnaire.getInvitation().reopenSubmittedFromApproved();
        auditLogService.recordSystemSuccess(
                AuditAction.QUESTIONNAIRE_APPROVAL_CANCELLED,
                QUESTIONNAIRE_ENTITY_TYPE,
                questionnaire.getId(),
                "Questionnaire approval cancelled"
        );
        return toDetailResponse(questionnaire);
    }

    @Transactional(readOnly = true)
    public void sendToXpm(Long id) {
        Questionnaire questionnaire = findQuestionnaire(id);
        if (questionnaire.getInvitation().getStatus() != InvitationStatus.APPROVED) {
            throw new BadRequestException("Questionnaire can only be sent to XPM from APPROVED status");
        }
        throw new NotImplementedException("Xero Practice Manager integration is not available yet.");
    }

    private Questionnaire findQuestionnaire(Long id) {
        return repository.findByIdWithInvitation(id)
                .orElseThrow(() -> new NotFoundException("Questionnaire not found"));
    }

    private AdminQuestionnaireSummaryResponse toSummaryResponse(Questionnaire questionnaire) {
        OnboardingInvitation invitation = questionnaire.getInvitation();
        return new AdminQuestionnaireSummaryResponse(
                questionnaire.getId(),
                questionnaire.getClientType(),
                invitation.getId(),
                invitation.getPreferredName(),
                invitation.getEmail(),
                questionnaire.getFirstName(),
                questionnaire.getMiddleName(),
                questionnaire.getLastName(),
                questionnaire.getEmail(),
                questionnaire.getMobilePhone(),
                questionnaire.getSuburb(),
                questionnaire.getState(),
                questionnaire.getPostcode(),
                questionnaire.getSubmittedAt(),
                invitation.getStatus(),
                invitation.getApprovedAt(),
                invitation.getXpmSentAt()
        );
    }

    private AdminQuestionnaireDetailResponse toDetailResponse(Questionnaire questionnaire) {
        OnboardingInvitation invitation = questionnaire.getInvitation();
        return new AdminQuestionnaireDetailResponse(
                questionnaire.getId(),
                questionnaire.getClientType(),
                invitation.getId(),
                invitation.getPreferredName(),
                invitation.getEmail(),
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
                invitation.getStatus(),
                invitation.getApprovedAt(),
                invitation.getXpmSentAt()
        );
    }
}
