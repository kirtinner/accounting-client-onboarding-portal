package com.kzhastkou.accountingonboarding.client.entity;

import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 50)
    private ClientType clientType;

    @Size(max = 255)
    @Column(name = "xpm_client_id")
    private String xpmClientId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_questionnaire_id", nullable = false, unique = true)
    private Questionnaire sourceQuestionnaire;

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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public String getXpmClientId() {
        return xpmClientId;
    }

    public Questionnaire getSourceQuestionnaire() {
        return sourceQuestionnaire;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void assignXpmClientId(String xpmClientId) {
        if (this.xpmClientId != null) {
            throw new IllegalStateException(
                    "XPM client ID has already been assigned."
            );
        }

        if (xpmClientId == null || xpmClientId.isBlank()) {
            throw new IllegalArgumentException(
                    "XPM client ID must not be blank."
            );
        }

        this.xpmClientId = xpmClientId.trim();
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

    protected Client() {
    }

    public Client(Questionnaire sourceQuestionnaire) {
        this.sourceQuestionnaire = Objects.requireNonNull(sourceQuestionnaire, "Source questionnaire must not be null");
        fillFrom(sourceQuestionnaire);
    }

    private void fillFrom(Questionnaire sourceQuestionnaire) {
        this.clientType = sourceQuestionnaire.getClientType();
        this.firstName = sourceQuestionnaire.getFirstName();
        this.middleName = sourceQuestionnaire.getMiddleName();
        this.lastName = sourceQuestionnaire.getLastName();
        this.dateOfBirth = sourceQuestionnaire.getDateOfBirth();
        this.email = sourceQuestionnaire.getEmail();
        this.mobilePhone = sourceQuestionnaire.getMobilePhone();
        this.addressLine1 = sourceQuestionnaire.getAddressLine1();
        this.addressLine2 = sourceQuestionnaire.getAddressLine2();
        this.suburb = sourceQuestionnaire.getSuburb();
        this.state = sourceQuestionnaire.getState();
        this.postcode = sourceQuestionnaire.getPostcode();
        this.country = sourceQuestionnaire.getCountry();
    }
}
