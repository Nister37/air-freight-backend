package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.loadplanning.domain.vo.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.vo.LoadCheck;
import be.kdg.airfreight.loadplanning.domain.vo.LoadRules;

import java.util.List;
import java.util.Objects;

/** Runs the configured checks against the same load data and current policy. */
public final class LoadChecks {
    private final List<LoadCheckRule> checks;
    private final LoadRules rules;

    public LoadChecks(List<LoadCheckRule> checks, LoadRules rules) {
        this.checks = List.copyOf(checks);
        this.rules = Objects.requireNonNull(rules);
    }

    public static LoadChecks standard(LoadRules rules) {
        return new LoadChecks(List.of(
                new LateralBalanceCheckRule(),
                new OffloadingCheckRule(),
                new UnplacedCargoCheckRule()), rules);
    }

    public List<LoadCheck> check(LoadView load, FloorPlan floorPlan) {
        return checks.stream().map(check -> check.check(load, floorPlan, rules)).toList();
    }
}
