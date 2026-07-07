# Questionnaire Specification

This document describes the planned Public Client Questionnaire feature for the Accounting Client Onboarding Portal. It is a specification only; implementation will be handled separately.

## Domain Decision

The onboarding flow uses this relationship:

```text
Practice
  -> OnboardingInvitation
  -> Questionnaire
```

`Questionnaire` is a separate business object from `OnboardingInvitation`.

## Business Responsibilities

### OnboardingInvitation

`OnboardingInvitation` owns invitation-specific data and lifecycle state:

- Invitation token
- Preferred name known by the practice
- Invitation email
- Client type
- Invitation lifecycle status
- Invitation timestamps

`preferredName` belongs to `OnboardingInvitation` and is the single source of truth for the name known by the practice. It may be displayed to the client from the invitation during the public questionnaire flow.

Do not store official first name, last name, address, or phone directly on `OnboardingInvitation`.

### Questionnaire

`Questionnaire` owns client-entered onboarding data:

- Official client details entered by the client
- Contact information
- Residential address
- Uploaded documents
- Client confirmation before submission

## Public Questionnaire Flow

1. Client opens a public link using the invitation token.
2. The link is valid only for `SENT` invitations that are not expired or cancelled.
3. Client fills the questionnaire in a wizard.
4. Client may go back and edit previous steps before final confirmation.
5. Final step is `Review & Confirm`.
6. Client must explicitly confirm that the provided information is accurate and complete.
7. After final submit:
   - Questionnaire is saved.
   - `OnboardingInvitation` status changes from `SENT` to `SUBMITTED`.
   - `submittedAt` is set.
   - Client can no longer edit the questionnaire in MVP.

## Wizard Steps

### 1. Personal Details

Fields:

- `firstName`
- `middleName` optional
- `lastName`
- `dateOfBirth`

### 2. Contact & Address

Contact fields:

- `email`
- `mobilePhone`

Residential address fields:

- `addressLine1`
- `addressLine2` optional
- `suburb`
- `state`
- `postcode`
- `country` default `Australia`

### 3. Documents

MVP scope:

- Document upload section is planned.
- Actual file upload may be implemented as a separate iteration.
- The UI should reserve a place for uploaded documents.

### 4. Review & Confirm

The final step shows:

- All entered data
- Uploaded documents list, if available
- Confirmation checkbox:

```text
I confirm that the information provided is accurate and complete.
```

The `Submit Questionnaire` button is disabled until the confirmation checkbox is checked.

## Validation Rules

Required fields:

- `firstName`
- `lastName`
- `dateOfBirth`
- `email`
- `mobilePhone`
- `addressLine1`
- `suburb`
- `state`
- `postcode`
- `country`

Format rules:

- `email` is required and must be valid.

Optional fields:

- `middleName`
- `addressLine2`

## Invitation Statuses

- `DRAFT`: Internal invitation draft; public questionnaire not available.
- `SENT`: Public questionnaire available and editable by client.
- `SUBMITTED`: Client submitted and confirmed questionnaire; no longer editable by client.
- `APPROVED`: Practice reviewed and approved.
- `XPM_SENT`: Client created or sent to Xero Practice Manager.
- `CANCELLED`: Invitation no longer usable.
- `EXPIRED`: Invitation no longer usable.

## Planned API Endpoints

### Public

Public endpoints are token-based and intended for client-facing questionnaire access.

- `GET /api/public/onboarding/{token}`
  - Loads invitation and questionnaire context for the public onboarding flow.
  - Valid only for usable token states.

- `PUT /api/public/onboarding/{token}/questionnaire`
  - Saves questionnaire data before final submission.
  - Intended for wizard progress updates in MVP.

- `POST /api/public/onboarding/{token}/submit`
  - Final questionnaire submission.
  - Requires client confirmation.
  - Changes invitation status from `SENT` to `SUBMITTED`.
  - Sets `submittedAt`.

### Internal

Internal endpoints are for practice users.

- `GET /api/invitations/{id}/questionnaire`
  - Loads submitted questionnaire details for practice review.

- `POST /api/invitations/{id}/approve`
  - Marks a submitted questionnaire as approved by the practice.

- `POST /api/invitations/{id}/send-to-xpm`
  - Sends or creates the approved client in Xero Practice Manager.

## Future Enhancements

- Add `NEEDS_CHANGES` status so the practice can return the questionnaire to the client for correction.
- Add document categories.
- Add draft auto-save.
- Add email notifications.
