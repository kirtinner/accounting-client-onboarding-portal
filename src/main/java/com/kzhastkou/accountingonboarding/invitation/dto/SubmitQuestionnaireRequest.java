package com.kzhastkou.accountingonboarding.invitation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitQuestionnaireRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 50) String phone,
        @NotBlank @Email String email,
        String residentialAddress,
        String notes
) {
}
