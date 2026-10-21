package be.kdg.airfreight.loadplanning.domain;

public interface LoadCheckRule {
    LoadCheck check(LoadView load, FloorPlan floorPlan, LoadRules rules);
}
