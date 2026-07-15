package com.agentry.core.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskBudgetTest {

    @Test
    void shouldRejectZeroLimit() {
        assertThrows(IllegalArgumentException.class, () -> new TaskBudget(0, 0));
    }

    @Test
    void shouldRejectNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () -> new TaskBudget(-1, 0));
    }

    @Test
    void shouldRejectSpentExceedingLimit() {
        assertThrows(IllegalArgumentException.class, () -> new TaskBudget(100, 150));
    }

    @Test
    void shouldReportCorrectRemaining() {
        var budget = new TaskBudget(1000, 300);
        assertEquals(700, budget.remaining());
    }

    @Test
    void shouldAllowIfCanAfford() {
        var budget = new TaskBudget(1000, 300);
        assertTrue(budget.canAfford(700));
        assertFalse(budget.canAfford(701));
    }

    @Test
    void shouldProduceNewBudgetAfterSpending() {
        var budget = new TaskBudget(1000, 0);
        var spent = budget.spend(400);
        assertEquals(400, spent.spent());
        assertEquals(600, spent.remaining());
    }
}
