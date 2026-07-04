# ADR-001: Project Standards

## Status

Accepted

## Context

The project needs stable naming standards before feature development begins. Consistent names reduce confusion across the repository, Spring configuration, database setup, Java packages, and future Docker-based local development.

## Decision

The project will use the following standards:

| Area | Standard |
| --- | --- |
| GitHub repository | `accounting-client-onboarding-portal` |
| Spring application name | `accounting-client-onboarding-portal` |
| PostgreSQL database | `accounting_onboarding` |
| Java base package | `com.kzhastkou.accountingonboarding` |
| Future Docker database container | `accounting-onboarding-db` |

## Consequences

- New code and documentation should use these names consistently.
- Future configuration, Docker files, and setup guides should align with these standards.
- Changes to these standards should require a new ADR or an update to this record with clear justification.
