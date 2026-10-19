package be.kdg.airfreight.operationscoordination.adapter.in.listeners;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.CargoFeedCommand;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record UldMessage(UUID messageId, String flightNumber, int departureDay,
                  String departureAirport, String uldCode, BigDecimal weight, String unit, String destination) {
    CargoFeedCommand command() {
        return new CargoFeedCommand(messageId, new FlightNumber(flightNumber), new IataDay(departureDay), new Airport(departureAirport),
                uldCode, weight, unit, new Airport(destination));
    }
}
