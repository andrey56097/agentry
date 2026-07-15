package com.agentry.core.agent;

import com.agentry.core.pipeline.TaskBudget;

public record AgentDefinition(
    String id,
    AgentRole role,
    String systemPromptTemplate,
    double temperature
) {}
