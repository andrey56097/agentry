---
name: project-structure
description: Current module layout, dependencies, and key conventions
metadata:
  type: reference
---

# Project Structure

## Modules (7 total)
- **agentry-core** — pure Java, no framework deps. Domain entities, agent interfaces, pipeline records.
- **agentry-persistence** — Spring Data JPA, Liquibase, PostgreSQL. JPA entities + migrations.
- **agentry-api** — Spring Web. REST endpoints + DTOs.
- **agentry-ci-gateway** — Spring Web. Webhook receiver POST /ci-callback.
- **agentry-cli** — Spring Shell. Console interface.
- **agentry-dashboard** — React host (future). Currently stub.
- **agentry-app** — Spring Boot entry point. All modules as dependencies.

## Key Decisions
- Gradle 8.12, Kotlin DSL, version catalog in gradle/libs.versions.toml
- Java 21 records for DTOs (no Lombok)
- PostgreSQL + Liquibase (not H2)
- 4 agent roles: Proposer → Tester → Critic → Improver
