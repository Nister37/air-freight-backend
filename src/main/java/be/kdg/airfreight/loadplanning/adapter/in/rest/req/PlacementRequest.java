package be.kdg.airfreight.loadplanning.adapter.in.rest.req;

public record PlacementRequest(String position) {
    public PlacementRequest {
        if (position == null) throw new IllegalArgumentException("ULD position is required");
    }
}
