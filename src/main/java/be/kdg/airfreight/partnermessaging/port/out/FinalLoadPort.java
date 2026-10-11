package be.kdg.airfreight.partnermessaging.port.out;

import be.kdg.airfreight.common.FlightId;

public interface FinalLoadPort {
    boolean flightExists(FlightId flightId);
    void processCPM(FlightId flightId);
}
