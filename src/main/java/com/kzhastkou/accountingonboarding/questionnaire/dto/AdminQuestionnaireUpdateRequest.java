package com.kzhastkou.accountingonboarding.questionnaire.dto;

import com.kzhastkou.accountingonboarding.common.model.ClientType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdminQuestionnaireUpdateRequest(
        @NotNull ClientType clientType,
        @NotBlank @Size(max = 255) String firstName,
        @Size(max = 255) String middleName,
        @NotBlank @Size(max = 255) String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 50) String mobilePhone,
        @NotBlank @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @NotBlank @Size(max = 255) String suburb,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Size(max = 20) String postcode,
        @NotBlank @Size(max = 100) String country
) {
}
