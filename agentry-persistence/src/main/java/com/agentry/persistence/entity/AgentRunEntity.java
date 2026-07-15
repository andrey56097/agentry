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
