package com.kzhastkou.accountingonboarding.questionnaire.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PublicQuestionnaireRequest(
        @Size(max = 255) String firstName,
        @Size(max = 255) String middleName,
        @Size(max = 255) String lastName,
        LocalDate dateOfBirth,
        @Email @Size(max = 255) String email,
        @Size(max = 50) String mobilePhone,
        @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @Size(max = 255) String suburb,
        @Size(max = 100) String state,
        @Size(max = 20) String postcode,
        @Size(max = 100) String country,
        boolean clientConfirmed
) {
}
