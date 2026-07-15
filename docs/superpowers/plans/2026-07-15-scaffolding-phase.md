# Agentry Scaffolding Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Создать компилируемый multi-module Gradle проект с AI-инфраструктурой (.claude/) и CI/CD (.github/workflows)

**Architecture:** 7 Gradle-модулей по feature-принципу: core (чистый Java) → persistence (JPA+Liquibase) → api/ci-gateway/cli/dashboard (Spring-приложения) → app (точка входа). Все модули собираются в один bootable jar.

**Tech Stack:** Java 21, Spring Boot 3.4.x, Gradle 8.12+ Kotlin DSL, PostgreSQL 16+, Liquibase, JUnit 5 + Mockito + Testcontainers, Pitest

## Global Constraints

- Java 21 — использовать records для DTO, pattern matching, text blocks. Lombok запрещён (ADR-3).
- Каждый модуль — отдельный `build.gradle.kts`, версии зависимостей — через `gradle/libs.versions.toml`
- Пакеты: `com.agentry.{module}` где module = core/persistence/api/cigateway/cli/dashboard/app
- Spring Boot plugin применяется ТОЛЬКО в `agentry-app`, остальные — библиотеки
- При сборке не должно быть WARNING про Lombok/annotation processors
- Все тесты проходят: `./gradlew test` должен зелёный (пустые тесты считаются пройденными)

---

### Task 1: Gradle Root Project Setup

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Delete: `src/Main.java` (старая заглушка)
- Update: `.gitignore`

**Interfaces:**
- Consumes: (none — first task)
- Produces: корневая Gradle-конфигурация, от которой зависят все модули

- [ ] **Step 1: Create `gradle/libs.versions.toml`**

```toml
[versions]
spring-boot = "3.4.1"
spring-dependency-management = "1.1.7"
java = "21"

# Database
postgresql = "42.7.4"
liquibase = "4.29.2"
hibernate = "6.6.4.Final"

# Testing
junit-jupiter = "5.11.4"
mockito = "5.14.2"
testcontainers = "1.20.4"

# Quality
pitest = "1.17.1"

# CLI
spring-shell = "3.4.0"

# LLM
anthropic-sdk = "0.2.0"
okhttp = "4.12.0"

[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-starter-data-jpa = { module = "org.springframework.boot:spring-boot-starter-data-jpa" }
spring-boot-starter-validation = { module = "org.springframework.boot:spring-boot-starter-validation" }
spring-boot-starter-test = { module = "org.springframework.boot:spring-boot-starter-test" }
spring-boot-starter-actuator = { module = "org.springframework.boot:spring-boot-starter-actuator" }

postgresql = { module = "org.postgresql:postgresql", version.ref = "postgresql" }
liquibase-core = { module = "org.liquibase:liquibase-core", version.ref = "liquibase" }
hibernate-core = { module = "org.hibernate.orm:hibernate-core", version.ref = "hibernate" }

junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }
mockito-core = { module = "org.mockito:mockito-core", version.ref = "mockito" }
mockito-junit-jupiter = { module = "org.mockito:mockito-junit-jupiter", version.ref = "mockito" }
testcontainers-postgresql = { module = "org.testcontainers:postgresql", version.ref = "testcontainers" }
testcontainers-junit-jupiter = { module = "org.testcontainers:junit-jupiter", version.ref = "testcontainers" }

spring-shell-starter = { module = "org.springframework.shell:spring-shell-starter", version.ref = "spring-shell" }

anthropic-sdk = { module = "com.anthropic:anthropic-sdk-java", version.ref = "anthropic-sdk" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version.ref = "spring-dependency-management" }
pitest = { id = "pl.project13.pitest", version = "0.4.0" }
```

- [ ] **Step 2: Create `gradle/wrapper/gradle-wrapper.properties`**

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.12-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 3: Create `gradle.properties`**

```properties
javaVersion=21
group=com.agentry
version=0.1.0-SNAPSHOT
```

- [ ] **Step 4: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "agentry"

include(
    "agentry-core",
    "agentry-persistence",
    "agentry-api",
    "agentry-ci-gateway",
    "agentry-cli",
    "agentry-dashboard",
    "agentry-app"
)
```

- [ ] **Step 5: Create `build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

group = "com.agentry"
version = "0.1.0-SNAPSHOT"

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        "testImplementation"(libs.junit.jupiter)
        "testImplementation"(libs.mockito.core)
        "testImplementation"(libs.mockito.junit.jupiter)
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
```

- [ ] **Step 6: Update `.gitignore`**

Проверить, что `.gitignore` уже есть и содержит минимум:
```gitignore
### IntelliJ IDEA ###
.idea/
*.iml
out/

### Gradle ###
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar

### Java ###
*.class

### Mac ###
.DS_Store

### Env ###
.env
.env.local
```

- [ ] **Step 7: Delete old stub + generate wrapper**

```bash
rm -f src/Main.java
# Проверить, есть ли gradlew; если нет — скачать wrapper
if [ ! -f gradlew ]; then
  gradle wrapper --gradle-version=8.12
fi
chmod +x gradlew
```

- [ ] **Step 8: Verify Gradle config parses**

```bash
./gradlew projects
```
Expected: список всех 7 модулей printed successfully

- [ ] **Step 9: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties gradle/ .gitignore
git rm -f src/Main.java
git commit -m "chore: scaffold Gradle multi-module project"
```

---

### Task 2: `agentry-core` Module

**Files:**
- Create: `agentry-core/build.gradle.kts`
- Create: `agentry-core/src/main/java/com/agentry/core/package-info.java`
- Create: `agentry-core/src/main/java/com/agentry/core/model/TaskStatus.java`
- Create: `agentry-core/src/main/java/com/agentry/core/agent/AgentRole.java`
- Create: `agentry-core/src/main/java/com/agentry/core/agent/AgentDefinition.java`
- Create: `agentry-core/src/main/java/com/agentry/core/pipeline/TaskBudget.java`
- Create: `agentry-core/src/main/java/com/agentry/core/pipeline/QualityGate.java`
- Create: `agentry-core/src/test/java/com/agentry/core/pipeline/TaskBudgetTest.java`
- Create: `agentry-core/src/test/java/com/agentry/core/pipeline/QualityGateTest.java`

**Interfaces:**
- Consumes: Gradle root config
- Produces: `TaskStatus`, `AgentRole` (enums); `AgentDefinition` (record); `TaskBudget`, `QualityGate` (records with logic)

- [ ] **Step 1: Create `agentry-core/build.gradle.kts`**

```kotlin
dependencies {
    // Pure Java module — no Spring dependencies
}
```

- [ ] **Step 2: Create `package-info.java`**

```java
@NullMarked
package com.agentry.core;

import org.jspecify.annotations.NullMarked;
```

Note: используем `org.jspecify:jspecify` для null-safety. Добавим в build.gradle.kts.

Update `agentry-core/build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.jspecify:jspecify:1.0.0")
}
```

- [ ] **Step 3: Create `TaskStatus.java`**

```java
package com.agentry.core.model;

public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    REVIEW,
    DONE
}
```

- [ ] **Step 4: Create `AgentRole.java`**

```java
package com.agentry.core.agent;

public enum AgentRole {
    PROPOSER,
    TESTER,
    CRITIC,
    IMPROVER
}
```

- [ ] **Step 5: Create `AgentDefinition.java`**

```java
package com.agentry.core.agent;

import com.agentry.core.model.TaskBudget;

public record AgentDefinition(
    String id,
    AgentRole role,
    String systemPromptTemplate,
    double temperature
) {}
```

- [ ] **Step 6: Create `TaskBudget.java`**

```java
package com.agentry.core.pipeline;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record TaskBudget(int totalLimit, int spent) {

    public TaskBudget {
        if (totalLimit <= 0) {
            throw new IllegalArgumentException("totalLimit must be positive, got: " + totalLimit);
        }
        if (spent < 0) {
            throw new IllegalArgumentException("spent must be non-negative, got: " + spent);
        }
        if (spent > totalLimit) {
            throw new IllegalArgumentException(
                "spent (" + spent + ") cannot exceed totalLimit (" + totalLimit + ")"
            );
        }
    }

    public int remaining() {
        return totalLimit - spent;
    }

    public boolean canAfford(int estimatedCost) {
        return remaining() >= estimatedCost;
    }

    public TaskBudget spend(int tokens) {
        return new TaskBudget(totalLimit, spent + tokens);
    }
}
```

- [ ] **Step 7: Create `QualityGate.java`**

```java
package com.agentry.core.pipeline;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record QualityGate(
    int requiredTestsPassPercent,
    int requiredMutationScore,
    int requiredLintErrors,
    int requiredCoveragePercent
) {
    public static QualityGate defaultGate() {
        return new QualityGate(100, 75, 0, 80);
    }

    public boolean isPassed(CiResult result) {
        return result.testsPassPercent() >= requiredTestsPassPercent
            && result.mutationScore() >= requiredMutationScore
            && result.lintErrors() <= requiredLintErrors
            && result.coveragePercent() >= requiredCoveragePercent;
    }

    public record CiResult(
        int testsPassPercent,
        int mutationScore,
        int lintErrors,
        int coveragePercent
    ) {}
}
```

- [ ] **Step 8: Create `TaskBudgetTest.java`**

```java
package com.agentry.core.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskBudgetTest {

    @Test
    void shouldRejectZeroLimit() {
        assertThrows(IllegalArgumentException.class, () -> new TaskBudget(0, 0));
    }

    @Test
    void shouldRejectNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () -> new TaskBudget(-1, 0));
    }

    @Test
    void shouldRejectSpentExceedingLimit() {
        assertThrows(IllegalArgumentException.class, () -> new TaskBudget(100, 150));
    }

    @Test
    void shouldReportCorrectRemaining() {
        var budget = new TaskBudget(1000, 300);
        assertEquals(700, budget.remaining());
    }

    @Test
    void shouldAllowIfCanAfford() {
        var budget = new TaskBudget(1000, 300);
        assertTrue(budget.canAfford(700));
        assertFalse(budget.canAfford(701));
    }

    @Test
    void shouldProduceNewBudgetAfterSpending() {
        var budget = new TaskBudget(1000, 0);
        var spent = budget.spend(400);
        assertEquals(400, spent.spent());
        assertEquals(600, spent.remaining());
    }
}
```

- [ ] **Step 9: Create `QualityGateTest.java`**

```java
package com.agentry.core.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QualityGateTest {

    @Test
    void shouldPassWhenAllMetricsMeetThresholds() {
        var gate = QualityGate.defaultGate();
        var result = new QualityGate.CiResult(100, 80, 0, 85);
        assertTrue(gate.isPassed(result));
    }

    @Test
    void shouldFailWhenTestsBelowThreshold() {
        var gate = QualityGate.defaultGate();
        var result = new QualityGate.CiResult(90, 80, 0, 85);
        assertFalse(gate.isPassed(result));
    }

    @Test
    void shouldFailWhenMutationScoreBelowThreshold() {
        var gate = QualityGate.defaultGate();
        var result = new QualityGate.CiResult(100, 70, 0, 85);
        assertFalse(gate.isPassed(result));
    }

    @Test
    void shouldFailWhenLintErrorsExceedThreshold() {
        var gate = QualityGate.defaultGate();
        var result = new QualityGate.CiResult(100, 80, 1, 85);
        assertFalse(gate.isPassed(result));
    }
}
```

- [ ] **Step 10: Run tests**

```bash
./gradlew :agentry-core:test
```
Expected: BUILD SUCCESSFUL, все тесты зелёные

- [ ] **Step 11: Commit**

```bash
git add agentry-core/
git commit -m "feat: add agentry-core module with domain entities and pipeline records"
```

---

### Task 3: `agentry-persistence` Module

**Files:**
- Create: `agentry-persistence/build.gradle.kts`
- Create: `agentry-persistence/src/main/java/com/agentry/persistence/entity/TaskEntity.java`
- Create: `agentry-persistence/src/main/java/com/agentry/persistence/entity/AgentRunEntity.java`
- Create: `agentry-persistence/src/main/java/com/agentry/persistence/entity/CodeVersionEntity.java`
- Create: `agentry-persistence/src/main/resources/db/changelog/db.changelog-master.xml`
- Create: `agentry-persistence/src/main/resources/db/changelog/v001_initial_schema.sql`

**Interfaces:**
- Consumes: (core entities from agentry-core)
- Produces: JPA-entities + Liquibase migrations

- [ ] **Step 1: Create `agentry-persistence/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(project(":agentry-core"))
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.liquibase.core)
    implementation(libs.postgresql)
    implementation(libs.hibernate.core)
    implementation(libs.spring.boot.starter.validation)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
}
```

- [ ] **Step 2: Create `TaskEntity.java`**

```java
package com.agentry.persistence.entity;

import com.agentry.core.model.TaskStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class TaskEntity {

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Column(name = "budget_limit", nullable = false)
    private int budgetLimit;

    @Column(name = "budget_spent", nullable = false)
    private int budgetSpent;

    @Column(name = "quality_gate_score")
    private Integer qualityGateScore;

    @Column(name = "git_branch", length = 255)
    private String gitBranch;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TaskEntity() {}

    public TaskEntity(UUID id, String description, TaskStatus status,
                      int budgetLimit, int budgetSpent, Instant createdAt) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.budgetLimit = budgetLimit;
        this.budgetSpent = budgetSpent;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public int getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(int budgetLimit) { this.budgetLimit = budgetLimit; }

    public int getBudgetSpent() { return budgetSpent; }
    public void setBudgetSpent(int budgetSpent) { this.budgetSpent = budgetSpent; }

    public Integer getQualityGateScore() { return qualityGateScore; }
    public void setQualityGateScore(Integer qualityGateScore) { this.qualityGateScore = qualityGateScore; }

    public String getGitBranch() { return gitBranch; }
    public void setGitBranch(String gitBranch) { this.gitBranch = gitBranch; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
```

- [ ] **Step 3: Create `AgentRunEntity.java`**

```java
package com.agentry.persistence.entity;

import com.agentry.core.agent.AgentRole;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_runs")
public class AgentRunEntity {

    @Id
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(nullable = false)
    private int round;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_role", nullable = false, length = 20)
    private AgentRole agentRole;

    @Column(name = "prompt_tokens", nullable = false)
    private int promptTokens;

    @Column(name = "completion_tokens", nullable = false)
    private int completionTokens;

    @Column(name = "cost_usd", nullable = false, precision = 10, scale = 6)
    private BigDecimal costUsd;

    @Column(name = "latency_ms", nullable = false)
    private int latencyMs;

    @Column
    private Integer score;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AgentRunEntity() {}

    public AgentRunEntity(UUID id, UUID taskId, int round, AgentRole agentRole,
                          int promptTokens, int completionTokens, BigDecimal costUsd,
                          int latencyMs, Instant createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.round = round;
        this.agentRole = agentRole;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.costUsd = costUsd;
        this.latencyMs = latencyMs;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public AgentRole getAgentRole() { return agentRole; }
    public void setAgentRole(AgentRole agentRole) { this.agentRole = agentRole; }

    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }

    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }

    public BigDecimal getCostUsd() { return costUsd; }
    public void setCostUsd(BigDecimal costUsd) { this.costUsd = costUsd; }

    public int getLatencyMs() { return latencyMs; }
    public void setLatencyMs(int latencyMs) { this.latencyMs = latencyMs; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 4: Create `CodeVersionEntity.java`**

```java
package com.agentry.persistence.entity;

import com.agentry.core.agent.AgentRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "code_versions")
public class CodeVersionEntity {

    @Id
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(nullable = false)
    private int round;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_role", nullable = false, length = 20)
    private AgentRole agentRole;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CodeVersionEntity() {}

    public CodeVersionEntity(UUID id, UUID taskId, int round, AgentRole agentRole,
                             String filePath, String contentHash, Instant createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.round = round;
        this.agentRole = agentRole;
        this.filePath = filePath;
        this.contentHash = contentHash;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public AgentRole getAgentRole() { return agentRole; }
    public void setAgentRole(AgentRole agentRole) { this.agentRole = agentRole; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 5: Create Liquibase master `db.changelog-master.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.29.xsd">

    <include file="v001_initial_schema.sql" relativeToChangelogFile="true"/>
</databaseChangeLog>
```

- [ ] **Step 6: Create initial migration `v001_initial_schema.sql`**

```sql
-- liquibase formatted sql

-- changeset author:1
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    budget_limit INT NOT NULL,
    budget_spent INT NOT NULL DEFAULT 0,
    quality_gate_score INT,
    git_branch VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE agent_runs (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id),
    round INT NOT NULL,
    agent_role VARCHAR(20) NOT NULL,
    prompt_tokens INT NOT NULL,
    completion_tokens INT NOT NULL,
    cost_usd DECIMAL(10,6) NOT NULL,
    latency_ms INT NOT NULL,
    score INT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE code_versions (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id),
    round INT NOT NULL,
    agent_role VARCHAR(20) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

- [ ] **Step 7: Verify compilation**

```bash
./gradlew :agentry-persistence:compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add agentry-persistence/
git commit -m "feat: add agentry-persistence module with JPA entities and Liquibase migrations"
```

---

### Task 4: `agentry-api` and `agentry-ci-gateway` Modules

**Files:**
- Create: `agentry-api/build.gradle.kts`
- Create: `agentry-api/src/main/java/com/agentry/api/package-info.java` (пустой stub)
- Create: `agentry-api/src/test/java/com/agentry/api/AgentryApiApplicationTest.java`
- Create: `agentry-ci-gateway/build.gradle.kts`
- Create: `agentry-ci-gateway/src/main/java/com/agentry/cigateway/package-info.java` (пустой stub)
- Create: `agentry-ci-gateway/src/test/java/com/agentry/cigateway/CiGatewayApplicationTest.java`

**Interfaces:**
- Consumes: agentry-core, agentry-persistence
- Produces: compile-ready Spring Boot library modules

- [ ] **Step 1: Create `agentry-api/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(project(":agentry-core"))
    implementation(project(":agentry-persistence"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    testImplementation(libs.spring.boot.starter.test)
}
```

- [ ] **Step 2: Create `agentry-api` test stub**

```java
package com.agentry.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
class AgentryApiApplicationTest {
    @Test
    void contextLoads() {
    }
}
```

Создаём также `TestConfig.java`:

```java
package com.agentry.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestConfig {}
```

- [ ] **Step 3: Create `agentry-ci-gateway/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(project(":agentry-core"))
    implementation(project(":agentry-persistence"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)

    testImplementation(libs.spring.boot.starter.test)
}
```

- [ ] **Step 4: Create `agentry-ci-gateway` test stub**

```java
package com.agentry.cigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
class CiGatewayApplicationTest {
    @Test
    void contextLoads() {
    }
}
```

Создаём `TestConfig.java`:

```java
package com.agentry.cigateway;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestConfig {}
```

- [ ] **Step 5: Verify compilation**

```bash
./gradlew :agentry-api:compileJava :agentry-ci-gateway:compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add agentry-api/ agentry-ci-gateway/
git commit -m "feat: add agentry-api and agentry-ci-gateway modules"
```

---

### Task 5: `agentry-cli` and `agentry-dashboard` Modules

**Files:**
- Create: `agentry-cli/build.gradle.kts`
- Create: `agentry-cli/src/main/java/com/agentry/cli/package-info.java` (stub)

- [ ] **Step 1: Create `agentry-cli/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(project(":agentry-core"))
    implementation(libs.spring.shell.starter)

    testImplementation(libs.spring.boot.starter.test)
}
```

- [ ] **Step 2: Create `agentry-dashboard/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(project(":agentry-api"))
    implementation(libs.spring.boot.starter.web)

    testImplementation(libs.spring.boot.starter.test)
}
```

- [ ] **Step 3: Create stub package-infos**

Create `agentry-cli/src/main/java/com/agentry/cli/package-info.java`:
```java
package com.agentry.cli;
```

Create `agentry-dashboard/src/main/java/com/agentry/dashboard/package-info.java`:
```java
package com.agentry.dashboard;
```

- [ ] **Step 4: Verify compilation**

```bash
./gradlew :agentry-cli:compileJava :agentry-dashboard:compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add agentry-cli/ agentry-dashboard/
git commit -m "feat: add agentry-cli and agentry-dashboard modules"
```

---

### Task 6: `agentry-app` Bootable Application Module

**Files:**
- Create: `agentry-app/build.gradle.kts`
- Create: `agentry-app/src/main/java/com/agentry/app/AgentryApplication.java`
- Create: `agentry-app/src/main/resources/application.yml`
- Create: `agentry-app/src/main/resources/application-dev.yml`
- Create: `agentry-app/src/main/resources/application-prod.yml`
- Create: `agentry-app/src/test/java/com/agentry/app/AgentryApplicationTest.java`

**Interfaces:**
- Consumes: все модули проекта
- Produces: исполняемый Spring Boot jar

- [ ] **Step 1: Create `agentry-app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":agentry-api"))
    implementation(project(":agentry-ci-gateway"))
    implementation(project(":agentry-cli"))
    implementation(project(":agentry-dashboard"))
    implementation(libs.spring.boot.starter.actuator)

    testImplementation(libs.spring.boot.starter.test)
}

springBoot {
    mainClass = "com.agentry.app.AgentryApplication"
}
```

- [ ] **Step 2: Create `AgentryApplication.java`**

```java
package com.agentry.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.agentry.core",
    "com.agentry.persistence",
    "com.agentry.api",
    "com.agentry.cigateway",
    "com.agentry.cli",
    "com.agentry.dashboard",
    "com.agentry.app"
})
@EntityScan(basePackages = "com.agentry.persistence.entity")
@EnableJpaRepositories(basePackages = "com.agentry.persistence.repository")
public class AgentryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentryApplication.class, args);
    }
}
```

- [ ] **Step 3: Create `application.yml`**

```yaml
spring:
  application:
    name: agentry

  datasource:
    url: jdbc:postgresql://localhost:5432/agentry
    username: ${AGENTRY_DB_USER:agentry}
    password: ${AGENTRY_DB_PASSWORD:agentry}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true

server:
  port: 8080

logging:
  level:
    com.agentry: DEBUG
    org.springframework: WARN
```

- [ ] **Step 4: Create `application-dev.yml`**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/agentry_dev

  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate

logging:
  level:
    com.agentry: DEBUG
    org.hibernate.SQL: DEBUG
```

- [ ] **Step 5: Create `application-prod.yml`**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${AGENTRY_DB_HOST}:${AGENTRY_DB_PORT}/${AGENTRY_DB_NAME}

logging:
  level:
    com.agentry: INFO
```

- [ ] **Step 6: Create `AgentryApplicationTest.java`**

```java
package com.agentry.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AgentryApplicationTest {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 7: Verify full compilation**

```bash
./gradlew :agentry-app:compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Build entire project**

```bash
./gradlew build -x test
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add agentry-app/
git commit -m "feat: add agentry-app bootable application module"
```

---

### Task 7: Claude Code AI Infrastructure

**Files:**
- Create: `.claude/CLAUDE.md`
- Create: `.claude/agents/spring-developer.md`
- Create: `.claude/agents/db-migrationist.md`
- Create: `.claude/skills/agentry-start.md`
- Create: `.claude/skills/agentry-test.md`
- Create: `.claude/skills/agentry-new-agent.md`
- Create: `.claude/memory/MEMORY.md`
- Create: `.claude/memory/project-structure.md`
- Create: `.claude/memory/arch-decisions.md`
- Create: `.claude/settings.local.json`

- [ ] **Step 1: Create `.claude/CLAUDE.md`**

```markdown
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
- `docs/superpowers/specs/2026-07-15-agentry-system-design.md` — full system spec
- `docs/superpowers/plans/2026-07-15-scaffolding-phase.md` — current implementation plan
```

- [ ] **Step 2: Create `.claude/agents/spring-developer.md`**

```markdown
---
name: spring-developer
description: Spring Boot expert — builds controllers, services, JPA repositories
---

You are a Spring Boot expert. You write clean, idiomatic Spring Boot 3.4+ code.

## Rules
- Use constructor injection (no @Autowired on fields)
- Controllers: REST only, @RestController, produce/consume application/json
- DTOs: use Java records
- Services: interface + implementation pattern
- Validation: jakarta.validation + @Valid in controllers
- Error handling: @RestControllerAdvice with proper HTTP status codes
- No Lombok (Java 21 records replace @Data/@Builder)
```

- [ ] **Step 3: Create `.claude/agents/db-migrationist.md`**

```markdown
---
name: db-migrationist
description: Liquibase and PostgreSQL expert — manages schema migrations
---

You are a database migration expert.

## Rules
- All schema changes go through Liquibase change logs
- Use SQL format (liquibase formatted sql), not XML
- Each migration is idempotent (use preconditions where needed)
- New tables get `CREATE TABLE IF NOT EXISTS` or Liquibase preconditions
- Indexes on foreign keys and frequently queried columns
- Never use Hibernate ddl-auto=update in production
```

- [ ] **Step 4: Create `.claude/skills/agentry-start.md`**

```markdown
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
```

- [ ] **Step 5: Create `.claude/skills/agentry-test.md`**

```markdown
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
```

- [ ] **Step 6: Create `.claude/skills/agentry-new-agent.md`**

```markdown
---
name: agentry-new-agent
description: Add a new AI agent role to the system
---

This skill guides you through adding a new agent role.

Steps:
1. Add to `AgentRole` enum in `agentry-core`
2. Create system prompt template in `agentry-core/src/main/resources/prompts/`
3. Add agent definition to the agent registry config
4. Add pipeline step in the orchestrator loop
5. Add tests for the new agent
```

- [ ] **Step 7: Create `.claude/memory/MEMORY.md`**

```markdown
- [Project Structure](project-structure.md) — current module layout and dependencies
- [Architecture Decisions](arch-decisions.md) — key ADRs and rationale
```

- [ ] **Step 8: Create `.claude/memory/project-structure.md`**

```markdown
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
```

- [ ] **Step 9: Create `.claude/memory/arch-decisions.md`**

```markdown
---
name: arch-decisions
description: Architecture Decision Records for the Agentry project
metadata:
  type: reference
---

# Architecture Decision Records

## ADR-1: Gradle Kotlin DSL (multi-module)
- **Why:** Type-safe config, better IDE support, industry standard for JVM multi-module
- **How:** Version catalog in gradle/libs.versions.toml, subproject convention in root build.gradle.kts

## ADR-2: PostgreSQL + Liquibase
- **Why:** Production-ready, strict migration control, rollback support
- **How:** SQL-format changelogs, ddl-auto=validate in all environments

## ADR-3: No Lombok
- **Why:** Lombok unnecessary with Java 21 records + pattern matching. Avoids annotation processor complexity.
- **How:** Java records for DTOs, explicit constructors/getters for JPA entities

## ADR-4: Feature-module layout
- **Why:** Clear boundaries for AI context windows, independent build/test per module
- **How:** 7 modules: core → persistence → api/ci-gateway/cli/dashboard → app

## ADR-5: Tester as independent agent
- **Why:** Proposer cannot objectively test own code. Tester with adversarial mindset improves quality.
- **How:** 4-role pipeline: Proposer writes code, Tester writes tests, Critic evaluates CI, Improver fixes
```

- [ ] **Step 10: Create `.claude/settings.local.json`**

```json
{
  "permissions": {
    "allow": [
      "Bash: curl",
      "Bash: ./gradlew",
      "Bash: git"
    ]
  }
}
```

- [ ] **Step 11: Commit**

```bash
git add .claude/
git commit -m "chore: add Claude Code AI infrastructure (CLAUDE.md, agents, skills, memory)"
```

---

### Task 8: GitHub Actions CI/CD

**Files:**
- Create: `.github/workflows/build.yml`

- [ ] **Step 1: Create `.github/workflows/build.yml`**

```yaml
name: Build & Test

on:
  push:
    branches: [ main, develop, 'feature/**', 'task/**' ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: agentry
          POSTGRES_USER: agentry
          POSTGRES_PASSWORD: agentry
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: gradle

      - name: Cache Gradle packages
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Compile
        run: ./gradlew compileJava compileTestJava --no-daemon

      - name: Run tests
        run: ./gradlew test --no-daemon
        env:
          AGENTRY_DB_USER: agentry
          AGENTRY_DB_PASSWORD: agentry
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/agentry
          SPRING_LIQUIBASE_ENABLED: false
          SPRING_JPA_HIBERNATE_DDL_AUTO: update

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: |
            **/build/reports/tests/
          retention-days: 7
```

- [ ] **Step 2: Commit**

```bash
git add .github/
git commit -m "ci: add GitHub Actions build workflow"
```

---

## Spec Coverage Check

| Spec section | Task(s) | Status |
|---|---|---|
| Gradle multi-module (ADR-1) | Task 1 | ✅ |
| Postgres + Liquibase (ADR-2) | Task 3 | ✅ |
| No Lombok (ADR-3) | Task 1 (gradle.properties), Task 2-6 | ✅ |
| Feature-module layout (ADR-4) | Tasks 2-6 | ✅ |
| Tester agent (ADR-5) | Task 2 (AgentRole enum) | ✅ |
| Module dependency graph | Tasks 2-6 (dependencies in build.gradle.kts) | ✅ |
| Domain entities (TaskStatus, AgentRole, TaskBudget, QualityGate) | Task 2 | ✅ |
| JPA entities (TaskEntity, AgentRunEntity, CodeVersionEntity) | Task 3 | ✅ |
| Liquibase changelog + initial schema | Task 3 | ✅ |
| application.yml + profiles | Task 6 | ✅ |
| .claude/ AI infrastructure | Task 7 | ✅ |
| GitHub Actions workflow | Task 8 | ✅ |

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-07-15-scaffolding-phase.md`.**

**Two execution options:**

1. **Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration
2. **Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
