# Roadmap

This roadmap describes the planned development direction. It may change as requirements become more precise.

## Phase 1: Project Foundation

- Establish project naming standards.
- Maintain clear documentation.
- Keep the Spring Boot baseline buildable and testable.
- Define initial architecture decisions.

## Phase 2: Core Domain

- Model invitation workflow.
- Model onboarding questionnaire.
- Model document upload process.
- Model onboarding status and lifecycle.
- Define validation rules for submitted client information.

## Phase 3: API and Persistence

- Implement REST endpoints for onboarding workflows.
- Implement domain persistence with Spring Data JPA.
- Manage database schema using Flyway migrations.
- Add focused service and repository tests.

## Phase 4: Security and Operations

- Configure authentication and authorization.
- Define role-based access for internal users and clients.
- Add operational configuration for local and future deployment environments.
- Improve logging and error handling.

## Phase 5: Portfolio Readiness

- Document API usage examples.
- Add representative test coverage.
- Provide local development setup instructions.
- Keep ADRs updated for important technical decisions.

## Phase 6: Xero Integration

- Implement OAuth2 authentication with Xero.
- Integrate with Xero Practice Manager API.
- Create approved clients in Xero Practice Manager.
- Handle integration errors and retries.
