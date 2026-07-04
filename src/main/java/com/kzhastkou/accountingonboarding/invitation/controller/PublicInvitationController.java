package com.kzhastkou.accountingonboarding.invitation.controller;

import com.kzhastkou.accountingonboarding.invitation.dto.InvitationResponse;
import com.kzhastkou.accountingonboarding.invitation.dto.QuestionnaireSubmissionResponse;
import com.kzhastkou.accountingonboarding.invitation.dto.SubmitQuestionnaireRequest;
import com.kzhastkou.accountingonboarding.invitation.service.InvitationService;
import com.kzhastkou.accountingonboarding.invitation.service.QuestionnaireSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/invitations")
public class PublicInvitationController {

    private final InvitationService invitationService;
    private final QuestionnaireSubmissionService submissionService;

    public PublicInvitationController(InvitationService invitationService,
                                      QuestionnaireSubmissionService submissionService) {
        this.invitationService = invitationService;
        this.submissionService = submissionService;
    }

    @GetMapping("/{token}")
    public InvitationResponse getInvitation(@PathVariable String token) {
        return invitationService.getPublicInvitation(token);
    }

    @PostMapping("/{token}/questionnaire")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionnaireSubmissionResponse submitQuestionnaire(@PathVariable String token,
                                                              @Valid @RequestBody SubmitQuestionnaireRequest request) {
        return submissionService.submit(token, request);
    }
}
