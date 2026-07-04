package com.kzhastkou.accountingonboarding.invitation.dto;

import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;

import java.time.LocalDateTime;

public record InvitationResponse(
        Long id,
        String token,
        String clientEmail,
        InvitationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        Long practiceId
) {
}
