package com.agentry.cli.service;

import com.agentry.core.agent.AgentRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

@Service
public class PromptLoader {

    private static final Logger log = LoggerFactory.getLogger(PromptLoader.class);
    private static final Map<AgentRole, String> PROMPT_PATHS = Map.of(
            AgentRole.PROPOSER, "prompts/proposer.txt",
            AgentRole.CRITIC, "prompts/critic.txt",
            AgentRole.IMPROVER, "prompts/improver.txt"
    );

    public String loadPrompt(AgentRole role) {
        String path = PROMPT_PATHS.get(role);
        if (path == null) {
            throw new IllegalArgumentException("No prompt template for role: " + role);
        }
        try {
            var resource = new ClassPathResource(path);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt: " + path, e);
        }
    }

    public String fillTemplate(String template, int remainingTokens) {
        return template.replace("{{remaining_tokens}}", String.valueOf(remainingTokens));
    }
}
