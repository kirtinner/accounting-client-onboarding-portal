package com.kzhastkou.accountingonboarding.invitation.entity;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "onboarding_invitations")
public class OnboardingInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID token;

    @Column(name = "preferred_name", nullable = false)
    private String preferredName;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 50)
    private ClientType clientType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InvitationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "xpm_sent_at")
    private Instant xpmSentAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_by")
    private String createdBy;

    protected OnboardingInvitation() {
    }

    public OnboardingInvitation(UUID token, String preferredName, String email, ClientType clientType,
                                InvitationStatus status, Instant createdAt, Instant expiresAt, String createdBy) {
        this.token = token;
        this.preferredName = preferredName;
        this.email = email;
        this.clientType = clientType;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public UUID getToken() {
        return token;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public String getEmail() {
        return email;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public Instant getXpmSentAt() {
        return xpmSentAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void updateDetails(String preferredName, String email, ClientType clientType) {
        this.preferredName = preferredName;
        this.email = email;
        this.clientType = clientType;
    }

    public void markSent(Instant sentAt) {
        this.status = InvitationStatus.SENT;
        this.sentAt = sentAt;
    }

    public void markCancelled(Instant cancelledAt) {
        this.status = InvitationStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    public void markSubmitted(Instant submittedAt) {
        this.status = InvitationStatus.SUBMITTED;
        this.submittedAt = submittedAt;
    }

    public void markApproved(Instant approvedAt) {
        if (status != InvitationStatus.SUBMITTED) {
            throw new BadRequestException("Questionnaire can only be approved from SUBMITTED status");
        }
        this.status = InvitationStatus.APPROVED;
        this.approvedAt = approvedAt;
    }

    public void reopenSubmittedFromApproved() {
        if (status != InvitationStatus.APPROVED) {
            throw new BadRequestException("Questionnaire can only be reopened from APPROVED status");
        }
        this.status = InvitationStatus.SUBMITTED;
        this.approvedAt = null;
    }
}
