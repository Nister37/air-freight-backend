package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.common.FlightFacts;

public interface ReceiveFlightFactsUseCase {
    void receiveFlightFacts(FlightFacts flightFacts);
}
