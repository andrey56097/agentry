package com.agentry.cli.service;

import com.agentry.core.agent.AgentRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

class PromptLoaderTest {

    private final PromptLoader promptLoader = new PromptLoader();

    @Test
    void shouldLoadProposerPrompt() {
        String prompt = promptLoader.loadPrompt(AgentRole.PROPOSER);
        assertNotNull(prompt);
        assertTrue(prompt.contains("Proposer"));
        assertTrue(prompt.contains("{{remaining_tokens}}"));
    }

    @Test
    void shouldLoadCriticPrompt() {
        String prompt = promptLoader.loadPrompt(AgentRole.CRITIC);
        assertNotNull(prompt);
        assertTrue(prompt.contains("Critic"));
    }

    @Test
    void shouldLoadImproverPrompt() {
        String prompt = promptLoader.loadPrompt(AgentRole.IMPROVER);
        assertNotNull(prompt);
        assertTrue(prompt.contains("Improver"));
    }

    @Test
    void shouldThrowForTesterRole() {
        assertThrows(IllegalArgumentException.class, () ->
                promptLoader.loadPrompt(AgentRole.TESTER)
        );
    }

    @Test
    void shouldFillTemplateBudget() {
        String template = "Budget: {{remaining_tokens}} tokens.";
        String filled = promptLoader.fillTemplate(template, 1500);
        assertEquals("Budget: 1500 tokens.", filled);
    }
}
