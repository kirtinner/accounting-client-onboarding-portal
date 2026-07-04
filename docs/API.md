# API

No REST API endpoints have been implemented yet.

This document describes the planned and implemented REST API for the Accounting Client Onboarding Portal.

The API is organized around the onboarding workflow rather than database entities.

## Planned API Areas

### Invitation Management

Endpoints for creating, sending, viewing, and managing onboarding invitations.

Typical operations:

- Create invitation
- Resend invitation
- Cancel invitation
- View invitation status

---

### Public Questionnaire

Public endpoints used by invited clients to submit onboarding information.

Typical operations:

- Retrieve questionnaire
- Submit questionnaire
- Update questionnaire
- Validate submitted information

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