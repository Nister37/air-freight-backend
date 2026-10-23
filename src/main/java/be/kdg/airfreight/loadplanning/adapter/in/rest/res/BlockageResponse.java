package be.kdg.airfreight.loadplanning.adapter.in.rest.res;

import java.util.List;

public record BlockageResponse(String uldCode, String position, List<BlockingUldResponse> blockers) { }
