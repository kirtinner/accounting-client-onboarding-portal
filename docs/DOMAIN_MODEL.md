# Domain Model

This document describes the initial domain model for the Accounting Client Onboarding Portal. It is intentionally small and focused on the first planned onboarding workflow.

## Naming Decisions

The system uses **Practice** instead of **Organization** because the primary business context is an Australian accounting or bookkeeping practice. This term is more natural for the target users and still leaves room for future multi-practice support.

## Practice

**Purpose:** Represents an accounting or bookkeeping practice using the system.

**Main fields:**

- `id`
- `name`
- `abn`
- `email`
- `phone`
- `createdAt`
- `updatedAt`

**Relationships:**

- Has many `Employee` records.
- Has many `Invitation` records.
- Has many `Client` records.

## Employee

**Purpose:** Represents a staff member of a practice who manages client onboarding.

**Main fields:**

- `id`
- `practiceId`
- `firstName`
- `lastName`
- `email`
- `role`
- `active`
- `createdAt`
- `updatedAt`

**Relationships:**

- Belongs to one `Practice`.
- May create many `Invitation` records.

## Invitation

**Purpose:** Represents an onboarding invitation sent to a prospective or new client.

**Main fields:**

- `id`
- `practiceId`
- `createdByEmployeeId`
- `clientEmail`
- `status`
- `token`
- `expiresAt`
- `acceptedAt`
- `createdAt`
- `updatedAt`

**Relationships:**

- Belongs to one `Practice`.
- May be created by one `Employee`.
- May result in one `Client`.
- May be linked to one `Questionnaire`.
- May be linked to many `UploadedDocument` records.

## Questionnaire

**Purpose:** Captures onboarding answers submitted by a client.

**Main fields:**

- `id`
- `invitationId`
- `status`
- `submittedAt`
- `createdAt`
- `updatedAt`

**Relationships:**

- Belongs to one `Invitation`.
- May belong to one `Client` after the invitation is accepted.

## UploadedDocument

**Purpose:** Represents a document uploaded during onboarding, such as identity, business, tax, or accounting records.

**Main fields:**

- `id`
- `invitationId`
- `clientId`
- `documentType`
- `fileName`
- `contentType`
- `storagePath`
- `uploadedAt`

**Relationships:**

- Belongs to one `Invitation`.
- May belong to one `Client` after onboarding is accepted.

## Client

**Purpose:** Represents a client business or individual onboarded by a practice.

**Main fields:**

- `id`
- `practiceId`
- `invitationId`
- `businessName`
- `abn`
- `contactFirstName`
- `contactLastName`
- `contactEmail`
- `status`
- `createdAt`
- `updatedAt`

**Relationships:**

- Belongs to one `Practice`.
- May originate from one `Invitation`.
- May have one `Questionnaire`.
- May have many `UploadedDocument` records.
