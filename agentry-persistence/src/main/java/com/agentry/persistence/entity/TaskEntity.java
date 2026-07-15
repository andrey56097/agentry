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
