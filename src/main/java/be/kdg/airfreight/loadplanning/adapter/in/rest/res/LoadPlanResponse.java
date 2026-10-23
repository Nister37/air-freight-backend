package be.kdg.airfreight.loadplanning.adapter.in.rest.res;

import be.kdg.airfreight.loadplanning.domain.enums.LoadStatus;

import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.domain.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public record LoadPlanResponse(LoadFacts load, FloorPlanResponse floorPlan, List<BlockageResponse> blockages,
                        boolean loadingWindowOpen) {
    public static LoadPlanResponse from(LoadPlan plan) {
        if (!(plan.floorPlan() instanceof MainDeckFloorPlan floor)) {
            throw new IllegalArgumentException("Only main-deck floor plans can be returned");
        }
        return new LoadPlanResponse(plan.load().facts(), new FloorPlanResponse(
                floor.rows().stream().map(Slot::value).toList(),
                floor.noseSlots().stream().map(Slot::value).toList(),
                floor.tailSlots().stream().map(Slot::value).toList(),
                floor.positions().stream().map(Position::code).toList(), floor.doorPosition().code()),
                plan.load().blockages(floor).stream().map(blockage -> new BlockageResponse(
                        blockage.uldCode().value(), blockage.position().code(),
                        blockage.blockers().stream().map(blocker -> new BlockingUldResponse(
                                blocker.code().value(), blocker.position().code())).toList())).toList(),
                plan.load().status() == LoadStatus.OPEN
                        && plan.load().loadingWindow().isOpenAt(LocalDateTime.now(ZoneOffset.UTC)));
    }

}
