package com.kzhastkou.accountingonboarding.invitation.dto;

import com.kzhastkou.accountingonboarding.invitation.entity.ClientType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOnboardingInvitationRequest(
        @NotBlank @Size(max = 255) String preferredName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull ClientType clientType
) {
}
