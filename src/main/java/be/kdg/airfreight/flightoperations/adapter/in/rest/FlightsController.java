package be.kdg.airfreight.flightoperations.adapter.in.rest;

import be.kdg.airfreight.flightoperations.adapter.in.rest.res.FlightResponse;

import be.kdg.airfreight.flightoperations.adapter.in.rest.req.ActualTimeRequest;
import be.kdg.airfreight.flightoperations.adapter.in.rest.req.CorrectDelayRequest;
import be.kdg.airfreight.flightoperations.adapter.in.rest.req.RegisterDelayRequest;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.enums.FlightStatus;
import be.kdg.airfreight.flightoperations.port.in.command.CorrectDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.command.RecordActualTimeCommand;
import be.kdg.airfreight.flightoperations.port.in.command.RegisterDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.command.RemoveDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.CorrectDelayUseCase;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightQuery;
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
    private final GetFlightQuery getFlight;
    private final RegisterDelayUseCase registerDelay;
    private final CorrectDelayUseCase correctDelay;
    private final RemoveDelayUseCase removeDelay;
    private final RecordDepartureUseCase recordDeparture;
    private final RecordArrivalUseCase recordArrival;

    FlightsController(
            FindFlightsQuery findFlights,
            GetFlightQuery getFlight,
            RegisterDelayUseCase registerDelay,
            CorrectDelayUseCase correctDelay,
            RemoveDelayUseCase removeDelay,
            RecordDepartureUseCase recordDeparture,
            RecordArrivalUseCase recordArrival) {
        this.findFlights = findFlights;
        this.getFlight = getFlight;
        this.registerDelay = registerDelay;
        this.correctDelay = correctDelay;
        this.removeDelay = removeDelay;
        this.recordDeparture = recordDeparture;
        this.recordArrival = recordArrival;
    }
    @GetMapping
    List<FlightResponse> list(@RequestParam(required = false) LocalDate date,
                              @RequestParam(required = false) String number,
                              @RequestParam(required = false) String departureAirport,
                              @RequestParam(required = false) String arrivalAirport,
                              @RequestParam(required = false) FlightStatus status) {
        return findFlights.findFlights(new FlightSelection(date, number == null ? null : new FlightNumber(number),
                departureAirport == null ? null : new Airport(departureAirport),
                arrivalAirport == null ? null : new Airport(arrivalAirport), status))
                .stream().map(FlightResponse::from).toList();
    }
    @GetMapping("/{number}/{date}/{airport}")
    FlightResponse get(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport) {
        return FlightResponse.from(getFlight.getFlight(id(number, date, airport)));
    }
    @PostMapping("/{number}/{date}/{airport}/delays")
    FlightResponse registerDelay(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                                 @RequestBody RegisterDelayRequest request) {
        RegisterDelayCommand command = new RegisterDelayCommand(id(number, date, airport), request.phase(),
                request.code(), request.minutes(), request.description());
        return FlightResponse.from(registerDelay.registerDelay(command));
    }
    @PutMapping("/{number}/{date}/{airport}/delays/{delayId}")
    FlightResponse correctDelay(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                                @PathVariable UUID delayId, @RequestBody CorrectDelayRequest request) {
        CorrectDelayCommand command = new CorrectDelayCommand(id(number, date, airport), delayId,
                request.minutes(), request.description());
        return FlightResponse.from(correctDelay.correctDelay(command));
    }
    @DeleteMapping("/{number}/{date}/{airport}/delays/{delayId}")
    FlightResponse removeDelay(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                               @PathVariable UUID delayId) {
        RemoveDelayCommand command = new RemoveDelayCommand(id(number, date, airport), delayId);
        return FlightResponse.from(removeDelay.removeDelay(command));
    }
    @PostMapping("/{number}/{date}/{airport}/departure")
    FlightResponse depart(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                          @RequestBody ActualTimeRequest request) {
        RecordActualTimeCommand command = new RecordActualTimeCommand(id(number, date, airport), request.actualTime());
        return FlightResponse.from(recordDeparture.recordDeparture(command));
    }
    @PostMapping("/{number}/{date}/{airport}/arrival")
    FlightResponse arrive(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                          @RequestBody ActualTimeRequest request) {
        RecordActualTimeCommand command = new RecordActualTimeCommand(id(number, date, airport), request.actualTime());
        return FlightResponse.from(recordArrival.recordArrival(command));
    }
    private FlightId id(String number, LocalDate date, String airport) {
        return new FlightId(new FlightNumber(number), date, new Airport(airport));
    }
}
