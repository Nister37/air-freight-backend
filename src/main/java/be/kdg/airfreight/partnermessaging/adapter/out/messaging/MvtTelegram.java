package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.partnermessaging.domain.enums.MessageKind;
import java.time.*;
import java.util.*;

/** The derived dates belong to this value and are not recomputed on redelivery. */
public record MvtTelegram(FlightNumber flightNumber, LocalDate scheduledDepartureDate,
                          AircraftRegistration aircraftRegistration, Airport airport,
                          MessageKind kind, LocalDateTime movementTime) {
    public MvtTelegram {
        Objects.requireNonNull(flightNumber);
        Objects.requireNonNull(scheduledDepartureDate);
        Objects.requireNonNull(aircraftRegistration);
        Objects.requireNonNull(airport);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(movementTime);
        if (!kind.isTelegram()) throw new IllegalArgumentException("MVT requires a telegram message kind");
    }
}
