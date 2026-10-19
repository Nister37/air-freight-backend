package be.kdg.airfreight.operationscoordination.port.in.command;

import be.kdg.airfreight.common.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record CargoFeedCommand(UUID messageId, FlightNumber flightNumber, IataDay departureDay,
                               Airport departureAirport, String uldCode, BigDecimal weight,
                               String unit, Airport destination) {
    public CargoFeedCommand {
        Objects.requireNonNull(messageId, "Message ID is required");
        Objects.requireNonNull(flightNumber, "Flight number is required");
        Objects.requireNonNull(departureDay, "Departure day is required");
        Objects.requireNonNull(departureAirport, "Departure airport is required");
        Objects.requireNonNull(uldCode, "ULD code is required");
        Objects.requireNonNull(weight, "Weight is required");
        Objects.requireNonNull(unit, "Weight unit is required");
        Objects.requireNonNull(destination, "Destination is required");
    }
}
