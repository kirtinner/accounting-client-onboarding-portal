package com.kzhastkou.accountingonboarding.invitation.controller;

import com.kzhastkou.accountingonboarding.invitation.dto.CreateOnboardingInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.dto.OnboardingInvitationResponse;
import com.kzhastkou.accountingonboarding.invitation.dto.UpdateOnboardingInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.service.OnboardingInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public List<OnboardingInvitationResponse> getInvitations(
            @RequestParam(required = false) List<InvitationStatus> statuses) {
        return service.getInvitations(statuses);
    }

    @GetMapping("/{id}")
    public OnboardingInvitationResponse getInvitation(@PathVariable Long id) {
        return service.getInvitation(id);
    }

    @PutMapping("/{id}")
    public OnboardingInvitationResponse updateInvitation(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateOnboardingInvitationRequest request) {
        return service.updateInvitation(id, request);
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
