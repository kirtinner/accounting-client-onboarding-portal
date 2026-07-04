package com.kzhastkou.accountingonboarding.invitation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "client_email", nullable = false)
    private String clientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InvitationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id")
    private Practice practice;

    protected Invitation() {
    }

    public Invitation(String token, String clientEmail, InvitationStatus status, LocalDateTime expiresAt,
                      LocalDateTime createdAt, Practice practice) {
        this.token = token;
        this.clientEmail = clientEmail;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.practice = practice;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Practice getPractice() {
        return practice;
    }
}
