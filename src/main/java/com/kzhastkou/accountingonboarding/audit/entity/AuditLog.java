package com.kzhastkou.accountingonboarding.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String actor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private AuditAction action;

    @Size(max = 50)
    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditResult result;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    protected AuditLog() {
    }

    public AuditLog(Instant occurredAt, String actor, AuditAction action, String entityType, Long entityId,
                    AuditResult result, String description) {
        this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred timestamp must not be null");
        this.actor = Objects.requireNonNull(actor, "Actor must not be null");
        this.action = Objects.requireNonNull(action, "Audit action must not be null");
        this.entityType = entityType;
        this.entityId = entityId;
        this.result = Objects.requireNonNull(result, "Audit result must not be null");
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getActor() {
        return actor;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public AuditResult getResult() {
        return result;
    }

    public String getDescription() {
        return description;
    }
}
