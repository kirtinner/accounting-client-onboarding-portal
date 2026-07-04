package com.kzhastkou.accountingonboarding.invitation.service;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.invitation.dto.QuestionnaireSubmissionResponse;
import com.kzhastkou.accountingonboarding.invitation.dto.SubmitQuestionnaireRequest;
import com.kzhastkou.accountingonboarding.invitation.entity.Invitation;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.QuestionnaireSubmission;
import com.kzhastkou.accountingonboarding.invitation.repository.InvitationRepository;
import com.kzhastkou.accountingonboarding.invitation.repository.QuestionnaireSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionnaireSubmissionService {

    private final QuestionnaireSubmissionRepository submissionRepository;
    private final InvitationRepository invitationRepository;
    private final InvitationService invitationService;

    public QuestionnaireSubmissionService(QuestionnaireSubmissionRepository submissionRepository,
                                          InvitationRepository invitationRepository,
                                          InvitationService invitationService) {
        this.submissionRepository = submissionRepository;
        this.invitationRepository = invitationRepository;
        this.invitationService = invitationService;
    }

    @Transactional
    public QuestionnaireSubmissionResponse submit(String token, SubmitQuestionnaireRequest request) {
        Invitation invitation = invitationService.getOpenInvitationByToken(token);

        if (submissionRepository.existsByInvitation(invitation)) {
            throw new BadRequestException("Questionnaire has already been submitted");
        }

        QuestionnaireSubmission submission = new QuestionnaireSubmission(
                invitation,
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.email(),
                request.residentialAddress(),
                request.notes(),
                LocalDateTime.now()
        );

        invitation.setStatus(InvitationStatus.COMPLETED);
        invitationRepository.save(invitation);

        return toResponse(submissionRepository.save(submission));
    }

    @Transactional(readOnly = true)
    public List<QuestionnaireSubmissionResponse> getSubmissions() {
        return submissionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private QuestionnaireSubmissionResponse toResponse(QuestionnaireSubmission submission) {
        return new QuestionnaireSubmissionResponse(
                submission.getId(),
                submission.getInvitation().getId(),
                submission.getFirstName(),
                submission.getLastName(),
                submission.getPhone(),
                submission.getEmail(),
                submission.getResidentialAddress(),
                submission.getNotes(),
                submission.getSubmittedAt()
        );
    }
}
