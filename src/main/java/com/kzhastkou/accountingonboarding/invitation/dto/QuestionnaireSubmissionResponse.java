package com.kzhastkou.accountingonboarding.invitation.dto;

import java.time.LocalDateTime;

public record QuestionnaireSubmissionResponse(
        Long id,
        Long invitationId,
        String firstName,
        String lastName,
        String phone,
        String email,
        String residentialAddress,
        String notes,
        LocalDateTime submittedAt
) {
}
