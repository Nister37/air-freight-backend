package be.kdg.airfreight.operationscoordination.adapter.in.rest;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.CorrectDelayCommand;
import be.kdg.airfreight.operationscoordination.port.in.command.RecordActualTimeCommand;
import be.kdg.airfreight.operationscoordination.port.in.command.RegisterDelayCommand;
import be.kdg.airfreight.operationscoordination.port.in.command.RemoveDelayCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.CorrectFlightDelayUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.GetOperationalFlightFactsUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.ListOperationalFlightsUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RecordFlightArrivalUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RecordFlightDepartureUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RegisterFlightDelayUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RemoveFlightDelayUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/flights")
@PreAuthorize("hasAnyAuthority('ROLE_flight-planner', 'ROLE_load-planner', 'ROLE_admin')")
class FlightsController {
    private final ListOperationalFlightsUseCase listFlights;
    private final GetOperationalFlightFactsUseCase getFlightFacts;
    private final RegisterFlightDelayUseCase registerDelay;
    private final CorrectFlightDelayUseCase correctDelay;
    private final RemoveFlightDelayUseCase removeDelay;
    private final RecordFlightDepartureUseCase recordDeparture;
    private final RecordFlightArrivalUseCase recordArrival;

    FlightsController(
            ListOperationalFlightsUseCase listFlights,
            GetOperationalFlightFactsUseCase getFlightFacts,
            RegisterFlightDelayUseCase registerDelay,
            CorrectFlightDelayUseCase correctDelay,
            RemoveFlightDelayUseCase removeDelay,
            RecordFlightDepartureUseCase recordDeparture,
            RecordFlightArrivalUseCase recordArrival) {
        this.listFlights = listFlights;
        this.getFlightFacts = getFlightFacts;
        this.registerDelay = registerDelay;
        this.correctDelay = correctDelay;
        this.removeDelay = removeDelay;
        this.recordDeparture = recordDeparture;
        this.recordArrival = recordArrival;
    }
    @GetMapping
    List<FlightFacts> list(@RequestParam(required = false) LocalDate date) { return listFlights.listFlights(date); }
    @GetMapping("/{number}/{date}/{airport}")
    FlightFacts get(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport) {
        return getFlightFacts.getFlightFacts(id(number, date, airport));
    }
    @PostMapping("/{number}/{date}/{airport}/delays")
    FlightFacts registerDelay(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                              @RequestBody RegisterDelayRequest request) {
        return registerDelay.registerDelay(new RegisterDelayCommand(id(number, date, airport), request.phase(), request.code(), request.minutes(), request.description()));
    }
    @PutMapping("/{number}/{date}/{airport}/delays/{delayId}")
    FlightFacts correctDelay(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                             @PathVariable UUID delayId, @RequestBody CorrectDelayRequest request) {
        return correctDelay.correctDelay(new CorrectDelayCommand(id(number, date, airport), delayId, request.minutes(), request.description()));
    }
    @DeleteMapping("/{number}/{date}/{airport}/delays/{delayId}")
    FlightFacts removeDelay(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                            @PathVariable UUID delayId) { return removeDelay.removeDelay(new RemoveDelayCommand(id(number, date, airport), delayId)); }
    @PostMapping("/{number}/{date}/{airport}/departure")
    FlightFacts depart(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                        @RequestBody ActualTimeRequest request) { return recordDeparture.recordDeparture(new RecordActualTimeCommand(id(number, date, airport), request.actualTime())); }
    @PostMapping("/{number}/{date}/{airport}/arrival")
    FlightFacts arrive(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                       @RequestBody ActualTimeRequest request) { return recordArrival.recordArrival(new RecordActualTimeCommand(id(number, date, airport), request.actualTime())); }
    private FlightId id(String number, LocalDate date, String airport) {
        return new FlightId(new FlightNumber(number), date, new Airport(airport));
    }
    record RegisterDelayRequest(DelayPhase phase, String code, int minutes, String description) {
        RegisterDelayRequest { if (phase == null) throw new IllegalArgumentException("Delay phase is required"); }
    }
    record CorrectDelayRequest(int minutes, String description) { }
    record ActualTimeRequest(LocalDateTime actualTime) {
        ActualTimeRequest { if (actualTime == null) throw new IllegalArgumentException("Actual UTC date and time is required"); }
    }
}
