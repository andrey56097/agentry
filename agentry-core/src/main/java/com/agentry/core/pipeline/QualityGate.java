package com.agentry.core.pipeline;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record QualityGate(
    int requiredTestsPassPercent,
    int requiredMutationScore,
    int requiredLintErrors,
    int requiredCoveragePercent
) {
    public static QualityGate defaultGate() {
        return new QualityGate(100, 75, 0, 80);
    }

    public boolean isPassed(CiResult result) {
        return result.testsPassPercent() >= requiredTestsPassPercent
            && result.mutationScore() >= requiredMutationScore
            && result.lintErrors() <= requiredLintErrors
            && result.coveragePercent() >= requiredCoveragePercent;
    }

    public record CiResult(
        int testsPassPercent,
        int mutationScore,
        int lintErrors,
        int coveragePercent
    ) {}
}
