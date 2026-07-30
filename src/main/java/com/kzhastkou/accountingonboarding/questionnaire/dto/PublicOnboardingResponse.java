package com.kzhastkou.accountingonboarding.questionnaire.dto;

import com.kzhastkou.accountingonboarding.common.model.ClientType;

import java.time.Instant;
import java.util.UUID;

public record PublicOnboardingResponse(
        Long invitationId,
        UUID token,
        String preferredName,
        String email,
        ClientType clientType,
        Instant expiresAt,
        QuestionnaireResponse questionnaire
) {
}
