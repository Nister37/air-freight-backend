package be.kdg.airfreight.partnermessaging.adapter.out.api;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.partnermessaging.port.out.FinalLoadPort;
import org.springframework.stereotype.Component;

@Component
class FlightFinalLoadAdapter implements FinalLoadPort {
    private final FlightOperationsApi flights;
    FlightFinalLoadAdapter(FlightOperationsApi flights) { this.flights = flights; }
    public boolean flightExists(FlightId id) { return flights.flightExists(id); }
    public void processCPM(FlightId id) { flights.processCPM(id); }
}
