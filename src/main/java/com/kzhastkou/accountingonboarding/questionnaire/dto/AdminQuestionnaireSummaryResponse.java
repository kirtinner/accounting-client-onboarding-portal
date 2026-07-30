package com.kzhastkou.accountingonboarding.questionnaire.dto;

import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;

import java.time.Instant;

public record AdminQuestionnaireSummaryResponse(
        Long questionnaireId,
        ClientType clientType,
        Long invitationId,
        String preferredName,
        String invitationEmail,
        String firstName,
        String middleName,
        String lastName,
        String email,
        String mobilePhone,
        String suburb,
        String state,
        String postcode,
        Instant submittedAt,
        InvitationStatus invitationStatus,
        Instant approvedAt,
        Instant xpmSentAt
) {
}
