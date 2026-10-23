package be.kdg.airfreight.flightoperations.adapter.in.api;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.FlightSchedule;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.flightoperations.port.in.usecase.AnnounceFlightsUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightExistsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightFactsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.ListFlightsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.ProcessCPMUseCase;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
class FlightOperationsApiAdapter implements FlightOperationsApi {
    private final AnnounceFlightsUseCase announceFlights;
    private final GetFlightFactsQuery getFlightFacts;
    private final FlightExistsQuery flightExists;
    private final ListFlightsQuery listFlights;
    private final ProcessCPMUseCase processCPM;

    FlightOperationsApiAdapter(
            AnnounceFlightsUseCase announceFlights,
            GetFlightFactsQuery getFlightFacts,
            FlightExistsQuery flightExists,
            ListFlightsQuery listFlights,
            ProcessCPMUseCase processCPM) {
        this.announceFlights = announceFlights;
        this.getFlightFacts = getFlightFacts;
        this.flightExists = flightExists;
        this.listFlights = listFlights;
        this.processCPM = processCPM;
    }

    @Override
    public List<FlightFacts> announceFlights(FlightSchedule schedule) {
        return announceFlights.announceFlights(schedule);
    }

    @Override
    public FlightFacts getFlightFacts(FlightId id) {
        return getFlightFacts.getFlightFacts(id);
    }

    @Override
    public boolean flightExists(FlightId id) {
        return flightExists.flightExists(id);
    }

    @Override
    public List<FlightFacts> listFlights(LocalDate date) {
        return listFlights.listFlights(date);
    }

    @Override
    public void processCPM(FlightId id) {
        processCPM.processCPM(id);
    }
}
