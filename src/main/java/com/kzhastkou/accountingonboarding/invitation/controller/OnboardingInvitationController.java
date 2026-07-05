package com.kzhastkou.accountingonboarding.invitation.controller;

import com.kzhastkou.accountingonboarding.invitation.dto.CreateOnboardingInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.dto.OnboardingInvitationResponse;
import com.kzhastkou.accountingonboarding.invitation.service.OnboardingInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
public class OnboardingInvitationController {

    private final OnboardingInvitationService service;

    public OnboardingInvitationController(OnboardingInvitationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OnboardingInvitationResponse createInvitation(@Valid @RequestBody CreateOnboardingInvitationRequest request) {
        return service.createInvitation(request);
    }

    @GetMapping
    public List<OnboardingInvitationResponse> getInvitations() {
        return service.getInvitations();
    }

    @GetMapping("/{id}")
    public OnboardingInvitationResponse getInvitation(@PathVariable Long id) {
        return service.getInvitation(id);
    }

    @PostMapping("/{id}/send")
    public OnboardingInvitationResponse sendInvitation(@PathVariable Long id) {
        return service.sendInvitation(id);
    }

    @PostMapping("/{id}/cancel")
    public OnboardingInvitationResponse cancelInvitation(@PathVariable Long id) {
        return service.cancelInvitation(id);
    }
}
