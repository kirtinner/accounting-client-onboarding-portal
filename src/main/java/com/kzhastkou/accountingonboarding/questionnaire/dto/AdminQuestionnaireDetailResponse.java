package com.kzhastkou.accountingonboarding.questionnaire.dto;

import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;

import java.time.Instant;
import java.time.LocalDate;

public record AdminQuestionnaireDetailResponse(
        Long questionnaireId,
        ClientType clientType,
        Long invitationId,
        String preferredName,
        String invitationEmail,
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
        InvitationStatus invitationStatus,
        Instant approvedAt,
        Instant xpmSentAt
) {
}
