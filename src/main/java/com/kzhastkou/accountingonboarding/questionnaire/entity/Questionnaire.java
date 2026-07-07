package com.kzhastkou.accountingonboarding.questionnaire.entity;

import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.questionnaire.dto.QuestionnaireRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "questionnaires")
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitation_id", nullable = false, unique = true)
    private OnboardingInvitation invitation;

    @NotBlank
    @Size(max = 255)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Size(max = 255)
    @Column(name = "middle_name")
    private String middleName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Size(max = 50)
    @Column(name = "mobile_phone", nullable = false, length = 50)
    private String mobilePhone;

    @NotBlank
    @Size(max = 255)
    @Column(name = "address_line_1", nullable = false)
    private String addressLine1;

    @Size(max = 255)
    @Column(name = "address_line_2")
    private String addressLine2;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String suburb;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String state;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String postcode;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String country = "Australia";

    @Column(name = "client_confirmed", nullable = false)
    private boolean clientConfirmed;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Questionnaire() {
    }

    public Questionnaire(OnboardingInvitation invitation, QuestionnaireRequest request, Instant createdAt) {
        this.invitation = invitation;
        this.createdAt = createdAt;
        updateFrom(request, createdAt);
    }

    public Long getId() {
        return id;
    }

    public OnboardingInvitation getInvitation() {
        return invitation;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getSuburb() {
        return suburb;
    }

    public String getState() {
        return state;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCountry() {
        return country;
    }

    public boolean isClientConfirmed() {
        return clientConfirmed;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateFrom(QuestionnaireRequest request, Instant updatedAt) {
        this.firstName = request.firstName();
        this.middleName = request.middleName();
        this.lastName = request.lastName();
        this.dateOfBirth = request.dateOfBirth();
        this.email = request.email();
        this.mobilePhone = request.mobilePhone();
        this.addressLine1 = request.addressLine1();
        this.addressLine2 = request.addressLine2();
        this.suburb = request.suburb();
        this.state = request.state();
        this.postcode = request.postcode();
        this.country = request.country();
        this.clientConfirmed = request.clientConfirmed();
        this.updatedAt = updatedAt;
    }

    public void submit(Instant submittedAt) {
        this.clientConfirmed = true;
        this.submittedAt = submittedAt;
        this.updatedAt = submittedAt;
    }
}
