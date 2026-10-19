package be.kdg.airfreight.loadplanning.adapter.in.rest;

public record PlacementRequest(String position) {
    public PlacementRequest {
        if (position == null) throw new IllegalArgumentException("ULD position is required");
    }
}
