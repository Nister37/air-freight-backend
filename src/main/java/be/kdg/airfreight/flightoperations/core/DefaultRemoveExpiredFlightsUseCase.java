package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.RemoveExpiredFlightsUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRemoveExpiredFlightsUseCase implements RemoveExpiredFlightsUseCase {
    private final FlightLoadPort loadPort;
    private final FlightLookup flightLookup;
    private final List<FlightUpdatePort> updatePorts;
    private final PolicyQuery policyQuery;

    DefaultRemoveExpiredFlightsUseCase(FlightLoadPort loadPort, FlightLookup flightLookup, List<FlightUpdatePort> updatePorts, PolicyQuery policyQuery) {
        this.loadPort = loadPort;
        this.flightLookup = flightLookup;
        this.updatePorts = List.copyOf(updatePorts);
        this.policyQuery = policyQuery;
    }

    @Override
    public void removeExpiredFlights() {
        LocalDate cutoff = LocalDate.now(ZoneOffset.UTC).minusDays(policyQuery.currentPolicy().flightRetentionDays());
        for (Flight expired : loadPort.loadExpired(cutoff)) {
            Flight flight = flightLookup.required(expired.facts().id());
            flight.remove();
            updatePorts.forEach(port -> port.remove(flight));
        }
    }

}
