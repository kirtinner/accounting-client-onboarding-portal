package com.kzhastkou.accountingonboarding.questionnaire.dto;

import java.time.Instant;
import java.time.LocalDate;

public record QuestionnaireResponse(
        Long id,
        Long invitationId,
        String firstName,
        String middleName,
        String lastName,
        LocalDate dateOfBirth,
        String email,
        String mobilePhone,
        String addressLine1,
        String addressLine2,
        String suburb,
        String state,
        String postcode,
        String country,
        boolean clientConfirmed,
        Instant submittedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
