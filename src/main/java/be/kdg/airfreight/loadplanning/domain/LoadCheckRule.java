package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.loadplanning.domain.vo.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.vo.LoadCheck;
import be.kdg.airfreight.loadplanning.domain.vo.LoadRules;

public interface LoadCheckRule {
    LoadCheck check(LoadView load, FloorPlan floorPlan, LoadRules rules);
}
