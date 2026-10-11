package be.kdg.airfreight.operationscoordination.port.out;

import be.kdg.airfreight.common.*;
import java.time.*;
import java.util.*;

public interface FlightRequestPort {
    List<FlightFacts> announceFlights(FlightSchedule schedule);
    FlightFacts registerDelay(FlightId id, DelayPhase phase, String code, int minutes, String description);
    FlightFacts correctDelay(FlightId id, UUID delayId, int minutes, String description);
    FlightFacts removeDelay(FlightId id, UUID delayId);
    FlightFacts recordDeparture(FlightId id, LocalDateTime actualTime);
    FlightFacts recordArrival(FlightId id, LocalDateTime actualTime);
    FlightFacts getFlightFacts(FlightId id);
    List<FlightFacts> listFlights(LocalDate scheduledDepartureDate);
}
