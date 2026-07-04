# Architecture

Accounting Client Onboarding Portal is a Spring Boot application designed to automate the client onboarding process for accounting firms.

The application is built around a business workflow rather than traditional CRUD operations. Its primary responsibility is to guide prospective clients through the onboarding process and prepare validated data for creation in Xero Practice Manager.

## Application Structure

The Java base package is:

```text
com.kzhastkou.accountingonboarding
```

Application code should be organized by business capability while keeping package names clear, consistent, and easy to navigate.

## Planned Layers

- **API Layer** – REST controllers and request/response DTOs.
- **Application Layer** – onboarding orchestration, business workflows, validation, and Xero integration services.
- **Domain Layer** – invitation, questionnaire, uploaded documents, onboarding workflow, and client lifecycle models.
- **Persistence Layer** – Spring Data JPA repositories and PostgreSQL-backed entities.
- **Security Layer** – authentication, authorization, and secure access to public and administrative endpoints.

## Domain Workflow

The onboarding workflow follows these stages:

1. An accountant creates an onboarding invitation.
2. The system generates a secure onboarding link.
3. The client completes the onboarding questionnaire.
4. The client uploads the required supporting documents.
5. An accountant reviews the submitted information.
6. After approval, the client is created in Xero Practice Manager.

## Persistence

The application uses PostgreSQL as its primary database.

Database name:

```text
accounting_onboarding
```

Database schema will be managed using Flyway migrations.

## Deployment Direction

The project is intended to support both local Docker-based development and future cloud deployment.

Reserved Docker database container name:

```text
accounting-onboarding-db
```

## Architecture Principles

The project follows several core engineering principles:

- Business workflow before CRUD.
- Clear separation between domain logic and integration logic.
- Small, focused services with a single responsibility.
- Documentation evolves together with the code.
- One logical change per commit.
- All generated code is reviewed before being committed.