package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.common.*;
import java.time.*;
import java.util.*;

public interface ListFlightsQuery {
    List<FlightFacts> listFlights(LocalDate scheduledDepartureDate);
}
