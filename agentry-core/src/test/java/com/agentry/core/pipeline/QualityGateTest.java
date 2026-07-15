package com.agentry.core.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QualityGateTest {

    @Test
    void shouldPassWhenAllMetricsMeetThresholds() {
        var gate = QualityGate.defaultGate();
        var result = new QualityGate.CiResult(100, 80, 0, 85);
        assertTrue(gate.isPassed(result));
    }

    @Test
    void shouldFailWhenTestsBelowThreshold() {
        var gate = QualityGate.defaultGate();
        var result = new QualityGate.CiResult(90, 80, 0, 85);
        assertFalse(gate.isPassed(result));
    }

    @Test
    void shouldFailWhenMutationScoreBelowThreshold() {
        var gate = QualityGate.defaultGate();
        var result = new QualityGate.CiResult(100, 70, 0, 85);
        assertFalse(gate.isPassed(result));
    }

    @Test
    void shouldFailWhenLintErrorsExceedThreshold() {
        var gate = QualityGate.defaultGate();
        var result = new QualityGate.CiResult(100, 80, 1, 85);
        assertFalse(gate.isPassed(result));
    }
}
