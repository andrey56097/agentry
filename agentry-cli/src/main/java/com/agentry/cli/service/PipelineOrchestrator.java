package com.agentry.cli.service;

import com.agentry.cli.llm.ProviderRegistry;
import com.agentry.core.agent.AgentRole;
import com.agentry.core.llm.LLMResponse;
import com.agentry.core.llm.ModelProvider;
import com.agentry.core.llm.ProviderType;
import com.agentry.core.model.TaskStatus;
import com.agentry.core.pipeline.QualityGate;
import com.agentry.core.pipeline.TaskBudget;
import com.agentry.persistence.entity.AgentRunEntity;
import com.agentry.persistence.entity.CodeVersionEntity;
import com.agentry.persistence.entity.TaskEntity;
import com.agentry.persistence.repository.AgentRunRepository;
import com.agentry.persistence.repository.CodeVersionRepository;
import com.agentry.persistence.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PipelineOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PipelineOrchestrator.class);
    private static final int MAX_ROUNDS = 10;
    private static final Pattern SCORE_PATTERN = Pattern.compile("\"score\"\\s*:\\s*(\\d+)");

    private final ProviderRegistry providerRegistry;
    private final PromptLoader promptLoader;
    private final TaskRepository taskRepository;
    private final AgentRunRepository agentRunRepository;
    private final CodeVersionRepository codeVersionRepository;
    private final QualityGate qualityGate;

    public PipelineOrchestrator(
            ProviderRegistry providerRegistry,
            PromptLoader promptLoader,
            TaskRepository taskRepository,
            AgentRunRepository agentRunRepository,
            CodeVersionRepository codeVersionRepository
    ) {
        this.providerRegistry = providerRegistry;
        this.promptLoader = promptLoader;
        this.taskRepository = taskRepository;
        this.agentRunRepository = agentRunRepository;
        this.codeVersionRepository = codeVersionRepository;
        this.qualityGate = QualityGate.defaultGate();
    }

    @Transactional
    public TaskEntity executeTask(String description, int budgetLimit) {
        return executeTask(description, budgetLimit, ProviderType.ANTHROPIC);
    }

    @Transactional
    public TaskEntity executeTask(String description, int budgetLimit, ProviderType providerType) {
        ModelProvider provider = providerRegistry.getProvider(providerType);
        UUID taskId = UUID.randomUUID();
        TaskBudget budget = new TaskBudget(budgetLimit, 0);
        int round = 1;

        TaskEntity task = new TaskEntity(
                taskId, description, TaskStatus.IN_PROGRESS,
                budgetLimit, 0, Instant.now()
        );
        taskRepository.save(task);
        log.info("Task {} created (provider: {}): {}", taskId, providerType, description);

        // Round 1: Proposer writes initial code
        log.info("Round 1: Proposer writing code...");
        LLMResponse proposerResponse = callAgent(
                provider, taskId, round, AgentRole.PROPOSER, 0.7,
                "Write code for the following task:\n\n" + description,
                budget
        );
        budget = budget.spend(proposerResponse.inputTokens() + proposerResponse.outputTokens());
        saveCodeVersion(taskId, round, AgentRole.PROPOSER, "round-1-proposer", proposerResponse.text());
        task.setBudgetSpent(budget.spent());
        taskRepository.save(task);

        String currentCode = proposerResponse.text();

        // Rounds 2+: Critic → Improver loop
        for (round = 2; round <= MAX_ROUNDS; round++) {
            if (!budget.canAfford(500)) {
                log.info("Budget exhausted after round {}. Stopping.", round - 1);
                break;
            }

            // Critic evaluates
            log.info("Round {}: Critic evaluating...", round);
            LLMResponse criticResponse = callAgent(
                    provider, taskId, round, AgentRole.CRITIC, 0.3,
                    "Evaluate this code:\n\n" + currentCode,
                    budget
            );
            budget = budget.spend(criticResponse.inputTokens() + criticResponse.outputTokens());

            int score = parseScore(criticResponse.text());
            log.info("Round {}: Critic score = {}", round, score);
            task.setQualityGateScore(score);
            task.setBudgetSpent(budget.spent());
            taskRepository.save(task);

            if (score >= 75) {
                log.info("Quality gate passed at round {}! Score: {}", round, score);
                break;
            }

            if (!budget.canAfford(500)) {
                log.info("Budget exhausted after critique. Final score: {}", score);
                break;
            }

            // Improver fixes code
            log.info("Round {}: Improver fixing code...", round);
            LLMResponse improverResponse = callAgent(
                    provider, taskId, round, AgentRole.IMPROVER, 0.5,
                    "Critic feedback:\n" + criticResponse.text()
                            + "\n\nCode to improve:\n" + currentCode,
                    budget
            );
            budget = budget.spend(improverResponse.inputTokens() + improverResponse.outputTokens());
            saveCodeVersion(taskId, round, AgentRole.IMPROVER, "round-" + round + "-improved", improverResponse.text());
            currentCode = improverResponse.text();
            task.setBudgetSpent(budget.spent());
            taskRepository.save(task);
        }

        task.setStatus(TaskStatus.DONE);
        taskRepository.save(task);
        log.info("Task {} completed. Total spent: {} / {} tokens", taskId, budget.spent(), budgetLimit);
        return task;
    }

    private LLMResponse callAgent(
            ModelProvider provider, UUID taskId, int round, AgentRole role,
            double temperature, String userMessage, TaskBudget budget
    ) {
        String template = promptLoader.loadPrompt(role);
        String prompt = promptLoader.fillTemplate(template, budget.remaining());
        LLMResponse response = provider.complete(prompt, userMessage, temperature);
        saveAgentRun(taskId, round, role, response);
        return response;
    }

    private void saveAgentRun(UUID taskId, int round, AgentRole role, LLMResponse response) {
        AgentRunEntity run = new AgentRunEntity(
                UUID.randomUUID(), taskId, round, role,
                response.inputTokens(), response.outputTokens(),
                BigDecimal.valueOf(response.costUsd().doubleValue()),
                (int) response.latencyMs(), Instant.now()
        );
        agentRunRepository.save(run);
    }

    private void saveCodeVersion(UUID taskId, int round, AgentRole role, String filePath, String content) {
        CodeVersionEntity version = new CodeVersionEntity(
                UUID.randomUUID(), taskId, round, role,
                filePath, hashContent(content), Instant.now()
        );
        codeVersionRepository.save(version);
    }

    private String hashContent(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash content", e);
        }
    }

    private int parseScore(String criticResponse) {
        var matcher = SCORE_PATTERN.matcher(criticResponse);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
