package com.agentry.persistence.repository;

import com.agentry.core.agent.AgentRole;
import com.agentry.persistence.entity.AgentRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentRunRepository extends JpaRepository<AgentRunEntity, UUID> {

    List<AgentRunEntity> findByTaskIdOrderByRoundAsc(UUID taskId);

    List<AgentRunEntity> findByTaskIdAndAgentRoleOrderByRoundAsc(UUID taskId, AgentRole agentRole);
}
