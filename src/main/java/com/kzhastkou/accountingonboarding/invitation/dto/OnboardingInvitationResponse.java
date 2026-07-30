package com.kzhastkou.accountingonboarding.invitation.dto;

import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;

import java.time.Instant;
import java.util.UUID;

public record OnboardingInvitationResponse(
        Long id,
        UUID token,
        String preferredName,
        String email,
        ClientType clientType,
        InvitationStatus status,
        Instant createdAt,
        Instant expiresAt,
        Instant sentAt,
        Instant submittedAt,
        Instant approvedAt,
        Instant xpmSentAt,
        Instant cancelledAt,
        String createdBy
) {
}
