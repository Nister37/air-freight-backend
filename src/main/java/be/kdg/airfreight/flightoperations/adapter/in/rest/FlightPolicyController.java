package be.kdg.airfreight.flightoperations.adapter.in.rest;

import be.kdg.airfreight.flightoperations.adapter.in.rest.req.UpdateFlightPolicyRequest;
import be.kdg.airfreight.flightoperations.adapter.in.rest.res.FlightPolicyResponse;

import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightPolicyQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.UpdateFlightPolicyUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights/policy")
public class FlightPolicyController {
    private final GetFlightPolicyQuery getPolicy;
    private final UpdateFlightPolicyUseCase updatePolicy;

    FlightPolicyController(GetFlightPolicyQuery getPolicy, UpdateFlightPolicyUseCase updatePolicy) {
        this.getPolicy = getPolicy;
        this.updatePolicy = updatePolicy;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_flight-planner', 'ROLE_load-planner', 'ROLE_admin')")
    FlightPolicyResponse get() {
        return FlightPolicyResponse.from(getPolicy.currentPolicy());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_admin')")
    FlightPolicyResponse update(@RequestBody UpdateFlightPolicyRequest request) {
        return FlightPolicyResponse.from(updatePolicy.updatePolicy(request.toCommand()));
    }
}
