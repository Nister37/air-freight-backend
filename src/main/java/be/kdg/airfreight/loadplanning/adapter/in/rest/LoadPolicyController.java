package be.kdg.airfreight.loadplanning.adapter.in.rest;

import be.kdg.airfreight.loadplanning.adapter.in.rest.req.UpdateLoadPolicyRequest;
import be.kdg.airfreight.loadplanning.adapter.in.rest.res.LoadPolicyResponse;

import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadPolicyQuery;
import be.kdg.airfreight.loadplanning.port.in.usecase.UpdateLoadPolicyUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loads/policy")
public class LoadPolicyController {
    private final GetLoadPolicyQuery getPolicy;
    private final UpdateLoadPolicyUseCase updatePolicy;

    LoadPolicyController(GetLoadPolicyQuery getPolicy, UpdateLoadPolicyUseCase updatePolicy) {
        this.getPolicy = getPolicy;
        this.updatePolicy = updatePolicy;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_flight-planner', 'ROLE_load-planner', 'ROLE_admin')")
    LoadPolicyResponse get() {
        return LoadPolicyResponse.from(getPolicy.currentPolicy());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_admin')")
    LoadPolicyResponse update(@RequestBody UpdateLoadPolicyRequest request) {
        return LoadPolicyResponse.from(updatePolicy.updatePolicy(request.toCommand()));
    }
}
