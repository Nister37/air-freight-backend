package be.kdg.airfreight.flightoperations.adapter.in.rest.req;

import java.time.LocalDateTime;

public record ActualTimeRequest(LocalDateTime actualTime) {
    public ActualTimeRequest {
        if (actualTime == null) throw new IllegalArgumentException("Actual UTC date and time is required");
    }
}
