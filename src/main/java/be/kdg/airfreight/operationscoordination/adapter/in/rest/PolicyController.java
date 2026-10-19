package be.kdg.airfreight.operationscoordination.adapter.in.rest;

import be.kdg.airfreight.common.OperationsPolicy;
import be.kdg.airfreight.operationscoordination.port.in.usecase.GetPolicyQuery;
import be.kdg.airfreight.operationscoordination.port.in.usecase.UpdatePolicyUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy")
public class PolicyController {
    private final GetPolicyQuery getPolicy;
    private final UpdatePolicyUseCase updatePolicy;
    PolicyController(GetPolicyQuery getPolicy, UpdatePolicyUseCase updatePolicy) {
        this.getPolicy = getPolicy;
        this.updatePolicy = updatePolicy;
    }
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_flight-planner', 'ROLE_load-planner', 'ROLE_admin')")
    OperationsPolicy get() { return getPolicy.currentPolicy(); }
    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_admin')")
    OperationsPolicy update(@RequestBody UpdatePolicyRequest request) { return updatePolicy.updatePolicy(request.toCommand()); }
}
