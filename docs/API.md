# API

This document describes the planned and implemented REST API for the Accounting Client Onboarding Portal.

The API is organized around the onboarding workflow rather than database entities.

## Implemented API Areas

### Invitation Management

Endpoints for creating, sending, viewing, and cancelling onboarding invitations.

#### Create Invitation

`POST /api/invitations`

Creates a draft onboarding invitation. The invitation stores a display/common name entered by the accountant, not the client's official first and last name.

Request body:

```json
{
  "preferredName": "Alex Smith",
  "email": "alex@example.com",
  "clientType": "INDIVIDUAL"
}
```

Response body:

```json
{
  "id": 1,
  "token": "00000000-0000-0000-0000-000000000000",
  "preferredName": "Alex Smith",
  "email": "alex@example.com",
  "clientType": "INDIVIDUAL",
  "status": "DRAFT",
  "createdAt": "2026-07-05T00:00:00Z",
  "expiresAt": "2026-07-15T00:00:00Z",
  "sentAt": null,
  "submittedAt": null,
  "approvedAt": null,
  "xpmSentAt": null,
  "cancelledAt": null,
  "createdBy": null
}
```

Validation rules:

- `preferredName` is required and must be at most 255 characters.
- `email` is required, must be a valid email address, and must be at most 255 characters.
- `clientType` is required. Supported values are `INDIVIDUAL` and `COMPANY`.

#### List Invitations

`GET /api/invitations`

Returns all onboarding invitations ordered by `createdAt` descending.

Response body:

```json
[
  {
    "id": 1,
    "token": "00000000-0000-0000-0000-000000000000",
    "preferredName": "Alex Smith",
    "email": "alex@example.com",
    "clientType": "INDIVIDUAL",
    "status": "DRAFT",
    "createdAt": "2026-07-05T00:00:00Z",
    "expiresAt": "2026-07-15T00:00:00Z",
    "sentAt": null,
    "submittedAt": null,
    "approvedAt": null,
    "xpmSentAt": null,
    "cancelledAt": null,
    "createdBy": null
  }
]
```

#### Get Invitation

`GET /api/invitations/{id}`

Returns one onboarding invitation by ID.

Error responses:

- `404 Not Found` if the invitation does not exist.

#### Send Invitation

`POST /api/invitations/{id}/send`

Marks a draft invitation as sent. This does not send a real email yet.

Status transition:

- `DRAFT` -> `SENT`

Error responses:

- `404 Not Found` if the invitation does not exist.
- `400 Bad Request` if the invitation is not in `DRAFT` status.

#### Cancel Invitation

`POST /api/invitations/{id}/cancel`

Cancels an invitation.

Allowed status transitions:

- `DRAFT` -> `CANCELLED`
- `SENT` -> `CANCELLED`

Error responses:

- `404 Not Found` if the invitation does not exist.
- `400 Bad Request` if the invitation is not in `DRAFT` or `SENT` status.

#### Status Transitions

Current MVP transitions:

```text
DRAFT -> SENT
DRAFT -> CANCELLED
SENT  -> CANCELLED
```

Future workflow statuses:

```text
SUBMITTED
APPROVED
XPM_SENT
EXPIRED
```

### Public Questionnaire

Public endpoints used by invited clients to submit onboarding information.

All public questionnaire endpoints are token based. The token must belong to a `SENT` invitation that has not expired. `DRAFT`, `SUBMITTED`, `APPROVED`, `XPM_SENT`, `CANCELLED`, and expired invitations cannot be edited through the public API.

#### Get Public Onboarding Context

`GET /api/public/onboarding/{token}`

Returns invitation context and the current questionnaire, if one has already been started.

Response body:

```json
{
  "invitationId": 1,
  "token": "00000000-0000-0000-0000-000000000000",
  "preferredName": "Alex Smith",
  "email": "alex@example.com",
  "clientType": "INDIVIDUAL",
  "expiresAt": "2026-07-15T00:00:00Z",
  "questionnaire": null
}
```

Error responses:

- `404 Not Found` if the token does not match an invitation.
- `400 Bad Request` if the invitation is not publicly editable.

#### Create or Update Questionnaire

`PUT /api/public/onboarding/{token}/questionnaire`

Creates or updates questionnaire data for a `SENT` invitation. This endpoint does not submit the questionnaire or change the invitation status.

Request body:

```json
{
  "firstName": "Alex",
  "middleName": null,
  "lastName": "Smith",
  "dateOfBirth": "1990-01-15",
  "email": "alex@example.com",
  "mobilePhone": "0400000000",
  "addressLine1": "1 Main Street",
  "addressLine2": null,
  "suburb": "Sydney",
  "state": "NSW",
  "postcode": "2000",
  "country": "Australia",
  "clientConfirmed": false
}
```

Error responses:

- `404 Not Found` if the token does not match an invitation.
- `400 Bad Request` if the invitation is not publicly editable.
- `400 Bad Request` for validation failures.

#### Submit Questionnaire

`POST /api/public/onboarding/{token}/submit`

Submits the existing questionnaire. The questionnaire must exist and `clientConfirmed` must be `true`.

Status transition:

- `SENT` -> `SUBMITTED`

Effects:

- Sets `questionnaires.submitted_at`.
- Sets `onboarding_invitations.submitted_at`.
- Changes invitation status to `SUBMITTED`.

Error responses:

- `404 Not Found` if the token does not match an invitation.
- `400 Bad Request` if the invitation is not publicly editable.
- `400 Bad Request` if the questionnaire does not exist.
- `400 Bad Request` if client confirmation is missing.

## Planned API Areas

---

### Document Upload

Endpoints for secure upload and management of onboarding documents.

Typical operations:

- Upload document
- List uploaded documents
- Download document
- Delete document

---

### Onboarding Administration

Endpoints used by accountants to review, approve, reject, and track onboarding progress.

Typical operations:

- Review submission
- Request additional information
- Approve onboarding
- Reject onboarding
- View onboarding history

---

### Xero Practice Manager Integration

Internal endpoints responsible for creating approved clients in Xero Practice Manager and monitoring integration status.

Typical operations:

- Create client in XPM
- View integration status
- Retry failed integration

---

## Documentation Standard

Each implemented endpoint should include:

- HTTP method and URL
- Purpose
- Authentication and authorization requirements
- Request body (if applicable)
- Response body
- Validation rules
- Possible error responses
- Example request
- Example response
