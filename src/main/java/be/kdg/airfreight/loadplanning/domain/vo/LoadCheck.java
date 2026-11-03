package be.kdg.airfreight.loadplanning.domain.vo;

import be.kdg.airfreight.loadplanning.domain.enums.CheckKind;
import be.kdg.airfreight.loadplanning.domain.enums.Result;
import be.kdg.airfreight.loadplanning.domain.enums.Severity;

import java.util.Objects;

public record LoadCheck(CheckKind kind, Severity severity, Result result, String explanation) {
    public LoadCheck {
        Objects.requireNonNull(kind);
        Objects.requireNonNull(severity);
        Objects.requireNonNull(result);
        Objects.requireNonNull(explanation);
    }

    public static LoadCheck passed(CheckKind kind, Severity severity, String explanation) {
        return new LoadCheck(kind, severity, Result.PASSED, explanation);
    }

    public static LoadCheck failed(CheckKind kind, Severity severity, String explanation) {
        return new LoadCheck(kind, severity, Result.FAILED, explanation);
    }

    /** Blocks finalising, no matter what. */
    public boolean isBlocking() {
        return result == Result.FAILED && severity == Severity.HARD;
    }

    /** Allowed only if the planner confirms the warnings. */
    public boolean isWarning() {
        return result == Result.FAILED && severity == Severity.SOFT;
    }
}


