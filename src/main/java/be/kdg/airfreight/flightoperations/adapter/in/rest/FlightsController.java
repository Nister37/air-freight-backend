package be.kdg.airfreight.flightoperations.adapter.in.rest;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.command.CorrectDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.command.RecordActualTimeCommand;
import be.kdg.airfreight.flightoperations.port.in.command.RegisterDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.command.RemoveDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.CorrectDelayUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightFactsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.FindFlightsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightSelection;
import be.kdg.airfreight.flightoperations.port.in.usecase.RecordArrivalUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.RecordDepartureUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.RegisterDelayUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.RemoveDelayUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/flights")
@PreAuthorize("hasAnyAuthority('ROLE_flight-planner', 'ROLE_load-planner', 'ROLE_admin')")
public class FlightsController {
    private final FindFlightsQuery findFlights;
    private final GetFlightFactsQuery getFlightFacts;
    private final RegisterDelayUseCase registerDelay;
    private final CorrectDelayUseCase correctDelay;
    private final RemoveDelayUseCase removeDelay;
    private final RecordDepartureUseCase recordDeparture;
    private final RecordArrivalUseCase recordArrival;

    FlightsController(
            FindFlightsQuery findFlights,
            GetFlightFactsQuery getFlightFacts,
            RegisterDelayUseCase registerDelay,
            CorrectDelayUseCase correctDelay,
            RemoveDelayUseCase removeDelay,
            RecordDepartureUseCase recordDeparture,
            RecordArrivalUseCase recordArrival) {
        this.findFlights = findFlights;
        this.getFlightFacts = getFlightFacts;
        this.registerDelay = registerDelay;
        this.correctDelay = correctDelay;
        this.removeDelay = removeDelay;
        this.recordDeparture = recordDeparture;
        this.recordArrival = recordArrival;
    }
    @GetMapping
    List<FlightFacts> list(@RequestParam(required = false) LocalDate date,
                           @RequestParam(required = false) String number,
                           @RequestParam(required = false) String departureAirport,
                           @RequestParam(required = false) String arrivalAirport,
                           @RequestParam(required = false) FlightStatus status) {
        return findFlights.findFlights(new FlightSelection(date, number == null ? null : new FlightNumber(number),
                departureAirport == null ? null : new Airport(departureAirport),
                arrivalAirport == null ? null : new Airport(arrivalAirport), status));
    }
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
}
