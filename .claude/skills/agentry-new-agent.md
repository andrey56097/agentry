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
