# Accounting Client Onboarding Portal

Accounting Client Onboarding Portal is a Spring Boot and React application being developed to automate onboarding for new accounting clients.

The long-term goal is to collect client information, supporting documents, and onboarding approvals before synchronising approved clients with Xero Practice Manager.

## Current MVP

The current MVP includes:

- Spring Boot backend application
- PostgreSQL integration
- Flyway database migrations
- Invitation Management backend API
- React + Vite Client Invitations page
- Global REST exception handling
- Xero OAuth 2.0 integration
- Multiple Xero organisation awareness
- Xero Accounting API contact and organisation endpoints

## Frontend

The frontend is a React + Vite application in `frontend/`.

The first MVP screen is the Client Invitations page:

```text
http://localhost:5173/
```

It supports:

- Listing onboarding invitations
- Creating draft invitations
- Sending draft invitations
- Cancelling draft or sent invitations

## Backend API

Invitation Management endpoints:

```text
POST /api/invitations
GET  /api/invitations
GET  /api/invitations/{id}
POST /api/invitations/{id}/send
POST /api/invitations/{id}/cancel
```

Xero diagnostic endpoints:

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
- React
- Vite
- Xero OAuth 2.0
- Xero Accounting API

## Running Locally

Prerequisites:

- Java 21
- PostgreSQL
- Node.js and npm
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

Start the backend:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Start the frontend:

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server runs on port `5173` and proxies `/api` requests to `http://localhost:8080`.

## Project Structure

```text
src
|-- main/java/com/kzhastkou/accountingonboarding
|   |-- common
|   |-- config
|   |-- invitation
|   `-- xero
`-- main/resources

frontend
`-- src
```

Project documentation is located under `docs/`.

## Documentation

- [Architecture](docs/Architecture.md)
- [API](docs/API.md)
- [Roadmap](docs/Roadmap.md)
- [Architecture Decision Records](docs/adr/README.md)

## Future Roadmap

Planned improvements include:

- Public onboarding form
- Secure document upload
- Authentication and authorisation
- Persistent OAuth token storage
- Xero Practice Manager integration
- Docker deployment
- Cloud deployment
- Expanded automated testing

## License

This project is licensed under the MIT License.
