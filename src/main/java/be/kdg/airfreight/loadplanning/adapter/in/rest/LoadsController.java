package be.kdg.airfreight.loadplanning.adapter.in.rest;

import be.kdg.airfreight.loadplanning.domain.LoadFacts;

import be.kdg.airfreight.loadplanning.adapter.in.rest.req.FinalisationRequest;
import be.kdg.airfreight.loadplanning.adapter.in.rest.req.PlacementRequest;
import be.kdg.airfreight.loadplanning.adapter.in.rest.res.LoadPlanResponse;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.loadplanning.port.in.command.*;
import be.kdg.airfreight.loadplanning.port.in.usecase.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loads/{number}/{date}/{airport}")
@PreAuthorize("hasAnyAuthority('ROLE_flight-planner', 'ROLE_load-planner', 'ROLE_admin')")
public class LoadsController {
    private final GetLoadQuery getLoad;
    private final PlaceULDUseCase placeULD;
    private final MoveULDUseCase moveULD;
    private final GetLoadPlanQuery getLoadPlan;
    private final RemovePlacementUseCase removePlacement;
    private final CheckLoadUseCase checkLoad;
    private final FinaliseLoadUseCase finaliseLoad;

    LoadsController(GetLoadQuery getLoad, PlaceULDUseCase placeULD, MoveULDUseCase moveULD, GetLoadPlanQuery getLoadPlan,
                    RemovePlacementUseCase removePlacement, CheckLoadUseCase checkLoad,
                    FinaliseLoadUseCase finaliseLoad) {
        this.getLoad = getLoad;
        this.placeULD = placeULD;
        this.moveULD = moveULD;
        this.getLoadPlan = getLoadPlan;
        this.removePlacement = removePlacement;
        this.checkLoad = checkLoad;
        this.finaliseLoad = finaliseLoad;
    }

    @GetMapping
    LoadFacts get(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport) {
        return getLoad.getLoad(id(number, date, airport));
    }

    @PutMapping("/placements/{uldCode}")
    LoadFacts place(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                    @PathVariable String uldCode, @RequestBody PlacementRequest request) {
        return placeULD.placeULD(new PlaceULDCommand(id(number, date, airport), uldCode, request.position()));
    }

    @DeleteMapping("/placements/{uldCode}")
    LoadFacts remove(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                     @PathVariable String uldCode) {
        return removePlacement.removePlacement(new RemovePlacementCommand(id(number, date, airport), uldCode));
    }

    @PatchMapping("/placements/{uldCode}")
    LoadFacts move(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                   @PathVariable String uldCode, @RequestBody PlacementRequest request) {
        return moveULD.moveULD(new MoveULDCommand(id(number, date, airport), uldCode, request.position()));
    }

    @GetMapping("/plan")
    LoadPlanResponse plan(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport) {
        return LoadPlanResponse.from(getLoadPlan.getLoadPlan(id(number, date, airport)));
    }

    @PostMapping("/checks")
    LoadFacts check(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport) {
        return checkLoad.checkLoad(id(number, date, airport));
    }

    @PostMapping("/finalisation")
    LoadFacts finalise(@PathVariable String number, @PathVariable LocalDate date, @PathVariable String airport,
                       @RequestBody FinalisationRequest request) {
        return finaliseLoad.finaliseLoad(new FinaliseLoadCommand(id(number, date, airport), request.confirmWarnings()));
    }

    private FlightId id(String number, LocalDate date, String airport) {
        return new FlightId(new FlightNumber(number), date, new Airport(airport));
    }

}
