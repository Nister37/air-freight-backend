package be.kdg.airfreight.operationscoordination.port.out;

import be.kdg.airfreight.common.*;
import java.util.*;

public interface FlightRequestPort {
    List<FlightFacts> announceFlights(FlightSchedule schedule);
}
