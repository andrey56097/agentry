package com.agentry.core.llm;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LLMResponseTest {

    @Test
    void shouldCreateResponse() {
        var response = new LLMResponse("Hello", 10, 20, new BigDecimal("0.00006"), 100L);
        assertEquals("Hello", response.text());
        assertEquals(10, response.inputTokens());
        assertEquals(20, response.outputTokens());
        assertEquals(0, new BigDecimal("0.00006").compareTo(response.costUsd()));
        assertEquals(100L, response.latencyMs());
    }

    @Test
    void shouldHandleEmptyText() {
        var response = new LLMResponse("", 0, 0, BigDecimal.ZERO, 0L);
        assertEquals("", response.text());
        assertTrue(response.costUsd().signum() == 0);
    }
}
