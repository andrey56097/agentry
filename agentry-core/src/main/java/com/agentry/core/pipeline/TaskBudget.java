package com.agentry.core.pipeline;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record TaskBudget(int totalLimit, int spent) {

    public TaskBudget {
        if (totalLimit <= 0) {
            throw new IllegalArgumentException("totalLimit must be positive, got: " + totalLimit);
        }
        if (spent < 0) {
            throw new IllegalArgumentException("spent must be non-negative, got: " + spent);
        }
        if (spent > totalLimit) {
            throw new IllegalArgumentException(
                "spent (" + spent + ") cannot exceed totalLimit (" + totalLimit + ")"
            );
        }
    }

    public int remaining() {
        return totalLimit - spent;
    }

    public boolean canAfford(int estimatedCost) {
        return remaining() >= estimatedCost;
    }

    public TaskBudget spend(int tokens) {
        return new TaskBudget(totalLimit, spent + tokens);
    }
}
