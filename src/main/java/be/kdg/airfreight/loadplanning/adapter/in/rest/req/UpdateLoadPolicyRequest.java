package be.kdg.airfreight.loadplanning.adapter.in.rest.req;

import be.kdg.airfreight.loadplanning.domain.vo.LoadPolicy;
import be.kdg.airfreight.loadplanning.port.in.command.UpdateLoadPolicyCommand;
import be.kdg.airfreight.loadplanning.domain.enums.Severity;
import java.math.BigDecimal;

public record UpdateLoadPolicyRequest(Severity offloadingConstraint, Integer loadingWindowOpeningMinutes,
                              Integer loadingWindowClosingMinutes, BigDecimal maximumLateralWeightDifferencePercent) {
    public UpdateLoadPolicyRequest {
        if (offloadingConstraint == null || loadingWindowOpeningMinutes == null || loadingWindowClosingMinutes == null || maximumLateralWeightDifferencePercent == null) {
            throw new IllegalArgumentException("All load policy settings are required");
        }
    }

    public UpdateLoadPolicyCommand toCommand() {
        return new UpdateLoadPolicyCommand(new LoadPolicy(offloadingConstraint, loadingWindowOpeningMinutes, loadingWindowClosingMinutes, maximumLateralWeightDifferencePercent));
    }
}
