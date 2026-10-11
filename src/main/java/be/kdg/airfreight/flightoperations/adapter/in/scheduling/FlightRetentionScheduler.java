package be.kdg.airfreight.flightoperations.adapter.in.scheduling;

import be.kdg.airfreight.flightoperations.port.in.usecase.RemoveExpiredFlightsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class FlightRetentionScheduler {
    private final RemoveExpiredFlightsUseCase useCase;
    FlightRetentionScheduler(RemoveExpiredFlightsUseCase useCase) { this.useCase = useCase; }
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 3_600_000)
    void removeExpired() { useCase.removeExpiredFlights(); }
}
