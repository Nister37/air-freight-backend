package be.kdg.airfreight.flightoperations.adapter.in.api;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.flightoperations.port.in.command.CorrectDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.command.RecordActualTimeCommand;
import be.kdg.airfreight.flightoperations.port.in.command.RegisterDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.command.RemoveDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.AnnounceFlightsUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.CorrectDelayUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightExistsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightFactsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.ListFlightsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.ProcessCPMUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.RecordArrivalUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.RecordDepartureUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.RegisterDelayUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.RemoveDelayUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.RemoveExpiredFlightsUseCase;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.*;

@Component
class FlightOperationsApiAdapter implements FlightOperationsApi {
    private final AnnounceFlightsUseCase announceFlights;
    private final RegisterDelayUseCase registerDelay;
    private final CorrectDelayUseCase correctDelay;
    private final RemoveDelayUseCase removeDelay;
    private final RecordDepartureUseCase recordDeparture;
    private final RecordArrivalUseCase recordArrival;
    private final GetFlightFactsQuery getFlightFacts;
    private final FlightExistsQuery flightExists;
    private final ListFlightsQuery listFlights;
    private final ProcessCPMUseCase processCPM;
    private final RemoveExpiredFlightsUseCase removeExpiredFlights;

    FlightOperationsApiAdapter(
            AnnounceFlightsUseCase announceFlights,
            RegisterDelayUseCase registerDelay,
            CorrectDelayUseCase correctDelay,
            RemoveDelayUseCase removeDelay,
            RecordDepartureUseCase recordDeparture,
            RecordArrivalUseCase recordArrival,
            GetFlightFactsQuery getFlightFacts,
            FlightExistsQuery flightExists,
            ListFlightsQuery listFlights,
            ProcessCPMUseCase processCPM,
            RemoveExpiredFlightsUseCase removeExpiredFlights) {
        this.announceFlights = announceFlights;
        this.registerDelay = registerDelay;
        this.correctDelay = correctDelay;
        this.removeDelay = removeDelay;
        this.recordDeparture = recordDeparture;
        this.recordArrival = recordArrival;
        this.getFlightFacts = getFlightFacts;
        this.flightExists = flightExists;
        this.listFlights = listFlights;
        this.processCPM = processCPM;
        this.removeExpiredFlights = removeExpiredFlights;
    }
    public List<FlightFacts> announceFlights(FlightSchedule schedule) { return announceFlights.announceFlights(schedule); }
    public FlightFacts registerDelay(FlightId id, DelayPhase phase, String code, int minutes, String description) {
        return registerDelay.registerDelay(new RegisterDelayCommand(id, phase, code, minutes, description));
    }
    public FlightFacts correctDelay(FlightId id, UUID delayId, int minutes, String description) {
        return correctDelay.correctDelay(new CorrectDelayCommand(id, delayId, minutes, description));
    }
    public FlightFacts removeDelay(FlightId id, UUID delayId) { return removeDelay.removeDelay(new RemoveDelayCommand(id, delayId)); }
    public FlightFacts recordDeparture(FlightId id, LocalDateTime at) { return recordDeparture.recordDeparture(new RecordActualTimeCommand(id, at)); }
    public FlightFacts recordArrival(FlightId id, LocalDateTime at) { return recordArrival.recordArrival(new RecordActualTimeCommand(id, at)); }
    public FlightFacts getFlightFacts(FlightId id) { return getFlightFacts.getFlightFacts(id); }
    public boolean flightExists(FlightId id) { return flightExists.flightExists(id); }
    public List<FlightFacts> listFlights(LocalDate date) { return listFlights.listFlights(date); }
    public void processCPM(FlightId id) { processCPM.processCPM(id); }
    public void removeExpiredFlights() { removeExpiredFlights.removeExpiredFlights(); }
}
