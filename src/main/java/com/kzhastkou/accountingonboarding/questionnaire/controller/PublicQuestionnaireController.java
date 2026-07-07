package com.kzhastkou.accountingonboarding.questionnaire.controller;

import com.kzhastkou.accountingonboarding.questionnaire.dto.PublicOnboardingResponse;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireRequest;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireResponse;
import com.kzhastkou.accountingonboarding.questionnaire.service.PublicQuestionnaireService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/onboarding")
public class PublicQuestionnaireController {

    private final PublicQuestionnaireService service;

    public PublicQuestionnaireController(PublicQuestionnaireService service) {
        this.service = service;
    }

    @GetMapping("/{token}")
    public PublicOnboardingResponse getOnboarding(@PathVariable String token) {
        return service.getOnboarding(token);
    }

    @PutMapping("/{token}/questionnaire")
    public QuestionnaireResponse createOrUpdateQuestionnaire(@PathVariable String token,
                                                            @Valid @RequestBody QuestionnaireRequest request) {
        return service.createOrUpdateQuestionnaire(token, request);
    }

    @PostMapping("/{token}/submit")
    public QuestionnaireResponse submitQuestionnaire(@PathVariable String token) {
        return service.submitQuestionnaire(token);
    }
}
