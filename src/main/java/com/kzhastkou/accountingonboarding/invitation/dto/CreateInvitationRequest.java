package com.kzhastkou.accountingonboarding.invitation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateInvitationRequest(
        @NotBlank @Email String clientEmail,
        @NotNull @Future LocalDateTime expiresAt,
        Long practiceId
) {
}
