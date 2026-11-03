package be.kdg.airfreight.flightoperations.adapter.in.api;

import be.kdg.airfreight.flightoperations.FlightDetails;
import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightExistsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.ProcessCPMUseCase;
import org.springframework.stereotype.Component;

@Component
class FlightOperationsApiAdapter implements FlightOperationsApi {
    private final GetFlightQuery getFlight;
    private final FlightExistsQuery flightExists;
    private final ProcessCPMUseCase processCPM;

    FlightOperationsApiAdapter(
            GetFlightQuery getFlight,
            FlightExistsQuery flightExists,
            ProcessCPMUseCase processCPM) {
        this.getFlight = getFlight;
        this.flightExists = flightExists;
        this.processCPM = processCPM;
    }

    @Override
    public FlightDetails getFlightDetails(FlightId id) {
        Flight flight = getFlight.getFlight(id);
        return new FlightDetails(flight.getId(), flight.getArrivalAirport(), flight.getAircraftRegistration(),
                flight.getScheduledDeparture(), flight.getEstimatedDeparture());
    }

    @Override
    public boolean flightExists(FlightId id) {
        return flightExists.flightExists(id);
    }

    @Override
    public void processCPM(FlightId id) {
        processCPM.processCPM(id);
    }
}
