package com.kzhastkou.accountingonboarding.questionnaire.controller;

import com.kzhastkou.accountingonboarding.questionnaire.dto.AdminQuestionnaireDetailResponse;
import com.kzhastkou.accountingonboarding.questionnaire.dto.AdminQuestionnaireSummaryResponse;
import com.kzhastkou.accountingonboarding.questionnaire.dto.AdminQuestionnaireUpdateRequest;
import com.kzhastkou.accountingonboarding.questionnaire.service.AdminQuestionnaireService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
public class AdminQuestionnaireController {

    private final AdminQuestionnaireService service;

    public AdminQuestionnaireController(AdminQuestionnaireService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminQuestionnaireSummaryResponse> getSubmittedQuestionnaires() {
        return service.getSubmittedQuestionnaires();
    }

    @GetMapping("/{id}")
    public AdminQuestionnaireDetailResponse getQuestionnaire(@PathVariable Long id) {
        return service.getQuestionnaire(id);
    }

    @PutMapping("/{id}")
    public AdminQuestionnaireDetailResponse updateQuestionnaire(@PathVariable Long id,
                                                               @Valid @RequestBody AdminQuestionnaireUpdateRequest request) {
        return service.updateQuestionnaire(id, request);
    }

    @PostMapping("/{id}/approve")
    public AdminQuestionnaireDetailResponse approveQuestionnaire(@PathVariable Long id) {
        return service.approveQuestionnaire(id);
    }

    @PostMapping("/{id}/reopen")
    public AdminQuestionnaireDetailResponse reopenQuestionnaire(@PathVariable Long id) {
        return service.reopenQuestionnaire(id);
    }

    @PostMapping("/{id}/send-to-xpm")
    public void sendToXpm(@PathVariable Long id) {
        service.sendToXpm(id);
    }
}
