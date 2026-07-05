# Accounting Client Onboarding Portal

Accounting Client Onboarding Portal is a Spring Boot application being developed to automate the onboarding of new clients for accounting firms.

The long-term goal is to collect client information, supporting documents, and onboarding approvals before automatically creating clients in Xero Practice Manager (XPM).

The current MVP demonstrates a complete integration with the Xero Accounting API, including OAuth 2.0 authentication, multi-tenant support, and live read/write operations against connected Xero organisations.

## Project Purpose

This project serves as a production-quality prototype demonstrating secure integration with the Xero platform in preparation for future Xero Practice Manager (XPM) integration.

## Project Goals

The application is intended to streamline the onboarding process by:

- Creating secure onboarding invitations
- Collecting client information through an online questionnaire
- Uploading supporting documents
- Reviewing onboarding submissions
- Approving new clients
- Automatically synchronising approved clients with Xero Practice Manager

## Current Development Status

The current MVP includes:

- Spring Boot backend application
- PostgreSQL integration
- Flyway database migrations
- Invitation and questionnaire backend foundation
- Global REST exception handling
- Xero OAuth 2.0 integration
- Multiple Xero organisation awareness
- Multi-tenant diagnostics
- Live Xero Accounting API integration
- Organisation information endpoint
- Contact retrieval
- Contact creation

## Xero Accounting API Integration

The application authenticates with Xero using OAuth 2.0 and performs live Accounting API requests.

### Implemented

- OAuth 2.0 Authorization Code Flow
- OAuth state validation
- Access token retrieval
- Refresh token retrieval
- Connected organisation discovery
- Multiple tenant awareness
- Connected organisation diagnostics
- Organisation endpoint
- Contact retrieval (GET)
- Contact creation (POST)
- Tenant-aware API responses

### Available Endpoints

```text
GET  /api/xero/connect
GET  /api/xero/status
GET  /api/xero/connections
GET  /api/xero/organisation
GET  /api/xero/contacts
POST /api/xero/contacts
```

OAuth tokens are currently stored in memory for the MVP prototype. Persistent token storage is planned for a future version.

## Technology Stack

- Java 21
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- JUnit
- Xero OAuth 2.0
- Xero Accounting API

## Running Locally

### Prerequisites

- Java 21
- PostgreSQL
- Maven Wrapper included in the repository

Create a PostgreSQL database:

```text
accounting_onboarding
```

Configure environment variables:

```text
POSTGRES_USER
POSTGRES_PASSWORD
XERO_CLIENT_ID
XERO_CLIENT_SECRET
XERO_REDIRECT_URI
```

`POSTGRES_USER` and `POSTGRES_PASSWORD` default to `postgres` for local development.

Start the application.

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

Connect a Xero organisation:

```text
http://localhost:8080/api/xero/connect
```

Verify the integration:

```text
http://localhost:8080/api/xero/status
http://localhost:8080/api/xero/connections
http://localhost:8080/api/xero/organisation
http://localhost:8080/api/xero/contacts
```

## Project Structure

```text
src
├── config
├── common
├── invitation
└── xero
```

Project documentation is located under:

```text
docs/
```

## Documentation

- [Architecture](docs/Architecture.md)
- [API](docs/API.md)
- [Roadmap](docs/Roadmap.md)
- [Architecture Decision Records](docs/adr/README.md)

## Future Roadmap

Planned improvements include:

- Xero Practice Manager integration
- Secure document upload
- Authentication and authorisation
- Administration UI
- Persistent OAuth token storage
- Docker deployment
- Cloud deployment
- Expanded automated testing

## License

Copyright © 2026 Kiryl Zhastkou.
All rights reserved.