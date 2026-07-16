package com.agentry.cli.llm;

import com.agentry.core.llm.LLMResponse;
import com.agentry.core.llm.ModelProvider;
import com.agentry.core.llm.ProviderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderRegistryTest {

    private ModelProvider anthropicProvider;
    private ModelProvider openaiProvider;
    private ProviderRegistry registry;

    @BeforeEach
    void setUp() {
        anthropicProvider = mock(ModelProvider.class);
        when(anthropicProvider.getType()).thenReturn(ProviderType.ANTHROPIC);

        openaiProvider = mock(ModelProvider.class);
        when(openaiProvider.getType()).thenReturn(ProviderType.OPENAI);

        registry = new ProviderRegistry(List.of(anthropicProvider, openaiProvider));
    }

    @Test
    void shouldReturnProviderByType() {
        var provider = registry.getProvider(ProviderType.ANTHROPIC);
        assertSame(anthropicProvider, provider);
    }

    @Test
    void shouldReturnDefaultAnthropicProvider() {
        var provider = registry.getDefaultProvider();
        assertSame(anthropicProvider, provider);
    }

    @Test
    void shouldThrowWhenProviderNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                registry.getProvider(ProviderType.OLLAMA)
        );
    }

    @Test
    void shouldReturnOptionalProvider() {
        assertTrue(registry.getOptionalProvider(ProviderType.OPENAI).isPresent());
        assertTrue(registry.getOptionalProvider(ProviderType.OLLAMA).isEmpty());
    }

    @Test
    void shouldDelegateCompletion() {
        var expected = new LLMResponse("test", 10, 20, new BigDecimal("0.0001"), 50L);
        when(anthropicProvider.complete("system", "user", 0.7)).thenReturn(expected);

        var result = registry.getProvider(ProviderType.ANTHROPIC)
                .complete("system", "user", 0.7);

        assertEquals(expected, result);
    }

    @Test
    void shouldThrowWhenNoProviders() {
        assertThrows(IllegalStateException.class, () ->
                new ProviderRegistry(List.of()).getDefaultProvider()
        );
    }
}
