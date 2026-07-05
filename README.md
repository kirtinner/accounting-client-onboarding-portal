# Accounting Client Onboarding Portal

Accounting Client Onboarding Portal is a Spring Boot application designed to automate client onboarding for accounting firms.

The project is intended to demonstrate professional Spring Boot backend development practices, including clear domain modeling, API design, persistence, security, documentation, and incremental delivery.

## Planned Business Workflow

The application will support a structured client onboarding workflow for accounting firms:

- An accountant creates a new onboarding invitation for a prospective client.
- The system generates a secure unique onboarding link.
- The client opens the link and completes the onboarding questionnaire.
- The client provides required personal, business, tax, and accounting information.
- The client uploads required supporting documents.
- An internal team member reviews the submitted information and documents.
- The onboarding submission is approved or returned for clarification.
- After approval, the client record can be created in Xero Practice Manager.

## Planned Tech Stack

- Java 21
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven
- JUnit and Spring test support
- Flyway (planned)
- Docker (planned)
- Xero Practice Manager API (planned)
- OAuth2 Client (Xero)

## Current Development Status

The repository currently contains the initial Spring Boot project structure and project standards documentation.

Application features, domain models, API endpoints, frontend code, and database migration strategy have not been implemented yet.

## Xero Integration Status

The application successfully connects to Xero using OAuth 2.0 and performs live Accounting API calls against a connected Xero organisation.

Implemented:
- OAuth 2.0 authorization flow
- Tenant connection retrieval
- Accounting API Contacts endpoint integration
- 
## Local Development Requirements

- Java 21
- Maven Wrapper included in the repository
- PostgreSQL available locally
- Planned local database name: `accounting_onboarding`
- Planned Spring application name: `accounting-client-onboarding-portal`

Run tests with:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

## Documentation

- [Architecture](docs/Architecture.md)
- [Roadmap](docs/Roadmap.md)
- [API](docs/API.md)
- [Architecture Decision Records](docs/adr/README.md)
