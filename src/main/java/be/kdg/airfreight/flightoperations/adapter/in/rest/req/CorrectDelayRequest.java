package be.kdg.airfreight.flightoperations.adapter.in.rest.req;

public record CorrectDelayRequest(int minutes, String description) {
    public CorrectDelayRequest {
        if (description == null) throw new IllegalArgumentException("Delay description is required");
    }
}
