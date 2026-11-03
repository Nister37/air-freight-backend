package be.kdg.airfreight.flightoperations.adapter.in.rest.res;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.flightoperations.domain.enums.DelayPhase;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.flightoperations.domain.enums.FlightStatus;
import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record FlightResponse(FlightId id, Airport arrivalAirport, AircraftRegistration aircraftRegistration,
                             LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival,
                             LocalDateTime estimatedDeparture, LocalDateTime estimatedArrival,
                             LocalDateTime actualDeparture, LocalDateTime actualArrival,
                             FlightStatus status, boolean finalLoadReceived, List<DelayResponse> delays) {
    public FlightResponse {
        delays = List.copyOf(delays);
    }

    public static FlightResponse from(Flight flight) {
        List<DelayResponse> delays = new ArrayList<>();
        flight.getDepartureDelays().forEach(delay -> delays.add(DelayResponse.from(delay, DelayPhase.DEPARTURE)));
        flight.getArrivalDelays().forEach(delay -> delays.add(DelayResponse.from(delay, DelayPhase.ARRIVAL)));
        return new FlightResponse(flight.getId(), flight.getArrivalAirport(), flight.getAircraftRegistration(),
                flight.getScheduledDeparture(), flight.getScheduledArrival(),
                flight.getEstimatedDeparture(), flight.getEstimatedArrival(),
                flight.getActualDeparture(), flight.getActualArrival(), flight.getStatus(),
                flight.isFinalLoadReceived(), delays);
    }
}
