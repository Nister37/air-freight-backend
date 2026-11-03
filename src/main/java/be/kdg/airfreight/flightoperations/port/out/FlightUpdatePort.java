package be.kdg.airfreight.flightoperations.port.out;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;

public interface FlightUpdatePort {
    void update(Flight flight);
    void remove(Flight flight);
}
