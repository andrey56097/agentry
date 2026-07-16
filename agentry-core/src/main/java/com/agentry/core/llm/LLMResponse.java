package com.agentry.core.llm;

import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;

@NullMarked
public record LLMResponse(
    String text,
    int inputTokens,
    int outputTokens,
    BigDecimal costUsd,
    long latencyMs
) {}
