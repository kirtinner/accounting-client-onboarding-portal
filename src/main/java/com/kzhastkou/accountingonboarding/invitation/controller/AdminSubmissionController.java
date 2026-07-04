package com.kzhastkou.accountingonboarding.invitation.controller;

import com.kzhastkou.accountingonboarding.invitation.dto.QuestionnaireSubmissionResponse;
import com.kzhastkou.accountingonboarding.invitation.service.QuestionnaireSubmissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/submissions")
public class AdminSubmissionController {

    private final QuestionnaireSubmissionService submissionService;

    public AdminSubmissionController(QuestionnaireSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping
    public List<QuestionnaireSubmissionResponse> getSubmissions() {
        return submissionService.getSubmissions();
    }
}
