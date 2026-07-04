package com.kzhastkou.accountingonboarding.invitation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "questionnaire_submissions")
public class QuestionnaireSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false, unique = true)
    private Invitation invitation;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 50)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column(name = "residential_address", columnDefinition = "text")
    private String residentialAddress;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    protected QuestionnaireSubmission() {
    }

    public QuestionnaireSubmission(Invitation invitation, String firstName, String lastName, String phone,
                                   String email, String residentialAddress, String notes,
                                   LocalDateTime submittedAt) {
        this.invitation = invitation;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.residentialAddress = residentialAddress;
        this.notes = notes;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public Invitation getInvitation() {
        return invitation;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
