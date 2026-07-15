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
