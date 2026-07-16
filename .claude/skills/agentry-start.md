---
name: agentry-start
description: Start the Agentry application locally
---

Start the Agentry Spring Boot application with dev profile:

Prerequisites: PostgreSQL running on localhost:5432

```bash
./gradlew :agentry-app:bootRun --args='--spring.profiles.active=dev'
```

To start with test profile (no DB needed if using Testcontainers):
```bash
./gradlew :agentry-app:bootRun --args='--spring.profiles.active=test'
```
