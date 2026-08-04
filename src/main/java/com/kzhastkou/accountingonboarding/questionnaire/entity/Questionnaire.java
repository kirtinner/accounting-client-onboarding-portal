package com.kzhastkou.accountingonboarding.questionnaire.entity;

import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.questionnaire.dto.AdminQuestionnaireUpdateRequest;
import com.kzhastkou.accountingonboarding.questionnaire.dto.PublicQuestionnaireRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 50)
    private ClientType clientType;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitation_id", nullable = false, unique = true)
    private OnboardingInvitation invitation;

    @Size(max = 255)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 255)
    @Column(name = "middle_name")
    private String middleName;

    @Size(max = 255)
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Email
    @Size(max = 255)
    @Column
    private String email;

    @Size(max = 50)
    @Column(name = "mobile_phone", length = 50)
    private String mobilePhone;

    @Size(max = 255)
    @Column(name = "address_line_1")
    private String addressLine1;

    @Size(max = 255)
    @Column(name = "address_line_2")
    private String addressLine2;

    @Size(max = 255)
    @Column
    private String suburb;

    @Size(max = 100)
    @Column(length = 100)
    private String state;

    @Size(max = 20)
    @Column(length = 20)
    private String postcode;

    @Size(max = 100)
    @Column(length = 100)
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

    public Questionnaire(OnboardingInvitation invitation, PublicQuestionnaireRequest request, Instant createdAt) {
        this.invitation = invitation;
        this.createdAt = createdAt;
        updateFrom(request, invitation.getClientType(), createdAt);
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

    public ClientType getClientType() { return clientType; }

    public void updateFrom(PublicQuestionnaireRequest request, ClientType clientType, Instant updatedAt) {
        this.clientType = clientType;
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

    public void updateFrom(AdminQuestionnaireUpdateRequest request, Instant updatedAt) {
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
        this.updatedAt = updatedAt;
    }

    public void submit(Instant submittedAt) {
        this.clientConfirmed = true;
        this.submittedAt = submittedAt;
        this.updatedAt = submittedAt;
    }
}
