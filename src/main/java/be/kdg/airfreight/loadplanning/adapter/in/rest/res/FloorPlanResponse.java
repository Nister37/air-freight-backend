package be.kdg.airfreight.loadplanning.adapter.in.rest.res;

import java.util.List;

public record FloorPlanResponse(List<String> rows, List<String> nosePositions, List<String> tailPositions,
                                List<String> positions, String doorPosition) { }
