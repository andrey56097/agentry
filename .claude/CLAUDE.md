# Agentry — Multi-Agent Code Review System

## Project Structure
Multi-module Gradle project (Kotlin DSL), Java 21, Spring Boot 3.4.x.

### Modules
- `agentry-core` — Pure Java domain entities, interfaces, pipeline records
- `agentry-persistence` — JPA entities, Spring Data repositories, Liquibase migrations, PostgreSQL
- `agentry-api` — REST controllers, DTOs, mappers
- `agentry-ci-gateway` — GitHub Actions webhooks (POST /ci-callback)
- `agentry-cli` — Spring Shell console interface
- `agentry-dashboard` — React frontend host + static (future)
- `agentry-app` — Spring Boot main class, config, entry point

### Build Commands
- `./gradlew build` — full build
- `./gradlew test` — all tests
- `./gradlew :agentry-core:test` — single module tests
- `./gradlew check` — tests + checks
- `./gradlew :agentry-app:bootRun` — run application

### Code Conventions
- **Java 21 features**: records for DTOs, text blocks, pattern matching
- **No Lombok**: explicit constructors, getters, setters (or records)
- **Packages**: `com.agentry.{module}.{layer}`
- **Testing**: JUnit 5 + Mockito, Testcontainers for DB tests
- **Database**: PostgreSQL, Liquibase for migrations, `ddl-auto: validate`

### Agent Roles (domain)
- **Proposer**: writes initial code
- **Tester**: writes tests (independent, adversarial mindset)
- **Critic**: evaluates CI metrics (real data, not text)
- **Improver**: rewrites code based on Critic feedback

### Key Files
- `docs/ai-agent-team-project-plan.md` — full system spec and roadmap
- `docs/superpowers/plans/2026-07-15-scaffolding-phase.md` — current implementation plan
