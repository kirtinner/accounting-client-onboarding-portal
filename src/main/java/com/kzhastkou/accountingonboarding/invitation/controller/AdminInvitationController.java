package com.kzhastkou.accountingonboarding.invitation.controller;

import com.kzhastkou.accountingonboarding.invitation.dto.CreateInvitationRequest;
import com.kzhastkou.accountingonboarding.invitation.dto.InvitationResponse;
import com.kzhastkou.accountingonboarding.invitation.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/invitations")
public class AdminInvitationController {

    private final InvitationService invitationService;

    public AdminInvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationResponse createInvitation(@Valid @RequestBody CreateInvitationRequest request) {
        return invitationService.createInvitation(request);
    }

    @GetMapping
    public List<InvitationResponse> getInvitations() {
        return invitationService.getInvitations();
    }
}
