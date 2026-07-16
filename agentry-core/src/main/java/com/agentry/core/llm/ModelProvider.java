package com.agentry.core.llm;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ModelProvider {

    ProviderType getType();

    LLMResponse complete(String systemPrompt, String userMessage, double temperature);

    default LLMResponse complete(String systemPrompt, String userMessage) {
        return complete(systemPrompt, userMessage, 0.7);
    }
}
