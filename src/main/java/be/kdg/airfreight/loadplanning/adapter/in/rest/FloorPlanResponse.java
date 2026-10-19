package be.kdg.airfreight.loadplanning.adapter.in.rest;

import java.util.List;

public record FloorPlanResponse(List<String> rows, List<String> nosePositions, List<String> tailPositions,
                                List<String> positions, String doorPosition) { }
