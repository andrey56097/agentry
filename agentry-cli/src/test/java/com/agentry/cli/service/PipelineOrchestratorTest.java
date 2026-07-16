package com.agentry.cli.service;

import com.agentry.cli.llm.ProviderRegistry;
import com.agentry.core.agent.AgentRole;
import com.agentry.core.llm.LLMResponse;
import com.agentry.core.llm.ModelProvider;
import com.agentry.core.llm.ProviderType;
import com.agentry.core.model.TaskStatus;
import com.agentry.persistence.repository.AgentRunRepository;
import com.agentry.persistence.repository.CodeVersionRepository;
import com.agentry.persistence.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineOrchestratorTest {

    @Mock
    private ProviderRegistry providerRegistry;
    @Mock
    private ModelProvider modelProvider;
    @Mock
    private PromptLoader promptLoader;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private AgentRunRepository agentRunRepository;
    @Mock
    private CodeVersionRepository codeVersionRepository;

    private PipelineOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        when(providerRegistry.getProvider(any())).thenReturn(modelProvider);
        when(promptLoader.loadPrompt(any())).thenReturn("System prompt {{remaining_tokens}}");
        when(promptLoader.fillTemplate(anyString(), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(agentRunRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(codeVersionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        orchestrator = new PipelineOrchestrator(
                providerRegistry, promptLoader,
                taskRepository, agentRunRepository, codeVersionRepository
        );
    }

    @Test
    void shouldRunProposerAndPassQualityGate() {
        when(modelProvider.complete(anyString(), contains("Write code"), eq(0.7)))
                .thenReturn(response("public class Hello {}", 100, 200));
        when(modelProvider.complete(anyString(), contains("Evaluate"), eq(0.3)))
                .thenReturn(response("{\"score\": 85, \"passed\": true}", 50, 30));

        var task = orchestrator.executeTask("Write Hello World", 5000);

        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals(85, task.getQualityGateScore());
    }

    @Test
    void shouldRunImproverWhenScoreBelowThreshold() {
        when(modelProvider.complete(anyString(), contains("Write code"), eq(0.7)))
                .thenReturn(response("public class Hello {}", 100, 200));
        when(modelProvider.complete(anyString(), contains("Evaluate"), eq(0.3)))
                .thenReturn(response("{\"score\": 50, \"passed\": false}", 50, 30))
                .thenReturn(response("{\"score\": 85, \"passed\": true}", 50, 30));
        when(modelProvider.complete(anyString(), contains("Critic feedback"), eq(0.5)))
                .thenReturn(response("public class ImprovedHello {}", 150, 250));

        var task = orchestrator.executeTask("Write Hello World", 10000);

        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals(85, task.getQualityGateScore());
        verify(modelProvider, atLeast(3)).complete(anyString(), anyString(), anyDouble());
    }

    @Test
    void shouldRespectBudgetLimit() {
        // Small budget — only Proposer runs, then budget exhaustion stops the pipeline
        when(modelProvider.complete(anyString(), contains("Write code"), eq(0.7)))
                .thenReturn(response("code", 10, 20));

        var task = orchestrator.executeTask("Hello", 200);

        assertTrue(task.getBudgetSpent() <= 200);
    }

    @Test
    void shouldConfigureProviderType() {
        when(providerRegistry.getProvider(ProviderType.OPENAI)).thenReturn(modelProvider);
        when(modelProvider.complete(anyString(), contains("Write code"), eq(0.7)))
                .thenReturn(response("code", 10, 20));
        when(modelProvider.complete(anyString(), contains("Evaluate"), eq(0.3)))
                .thenReturn(response("{\"score\": 80, \"passed\": true}", 10, 10));

        var task = orchestrator.executeTask("Hello", 5000, ProviderType.OPENAI);

        assertEquals(TaskStatus.DONE, task.getStatus());
        verify(providerRegistry).getProvider(ProviderType.OPENAI);
    }

    @Test
    void shouldStopAfterMaxRounds() {
        when(modelProvider.complete(anyString(), contains("Write code"), eq(0.7)))
                .thenReturn(response("code", 5, 10));
        when(modelProvider.complete(anyString(), contains("Evaluate"), eq(0.3)))
                .thenReturn(response("{\"score\": 50, \"passed\": false}", 5, 10));
        when(modelProvider.complete(anyString(), contains("Critic feedback"), eq(0.5)))
                .thenReturn(response("fixed code", 5, 10));

        var task = orchestrator.executeTask("Hello", 100000);

        assertEquals(TaskStatus.DONE, task.getStatus());
        // Should have run multiple rounds (Proposer + several Critic+Improver cycles)
        verify(modelProvider, atLeast(4)).complete(anyString(), anyString(), anyDouble());
    }

    private LLMResponse response(String text, int inTokens, int outTokens) {
        return new LLMResponse(text, inTokens, outTokens,
                new BigDecimal("0.0001"), 50L);
    }
}
