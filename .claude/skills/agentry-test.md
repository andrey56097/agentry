---
name: agentry-test
description: Run tests for a specific module or the whole project
---

Run all tests:
```bash
./gradlew test
```

Single module:
```bash
./gradlew :agentry-core:test
```

Specific test class:
```bash
./gradlew :agentry-core:test --tests "*TaskBudgetTest*"
```

With verbose output:
```bash
./gradlew test --info
```
