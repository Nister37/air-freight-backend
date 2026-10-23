package be.kdg.airfreight.loadplanning.adapter.in.rest.res;

import be.kdg.airfreight.loadplanning.domain.LoadPolicy;
import be.kdg.airfreight.loadplanning.domain.enums.Severity;
import java.math.BigDecimal;

public record LoadPolicyResponse(Severity offloadingConstraint, int loadingWindowOpeningMinutes,
                              int loadingWindowClosingMinutes, BigDecimal maximumLateralWeightDifferencePercent) {
    public static LoadPolicyResponse from(LoadPolicy policy) {
        return new LoadPolicyResponse(policy.offloadingConstraint(), policy.loadingWindowOpeningMinutes(), policy.loadingWindowClosingMinutes(), policy.maximumLateralWeightDifferencePercent());
    }
}
