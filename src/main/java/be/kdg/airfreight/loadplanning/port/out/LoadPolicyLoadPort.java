package be.kdg.airfreight.loadplanning.port.out;

import be.kdg.airfreight.loadplanning.domain.vo.LoadPolicy;
import java.util.Optional;

public interface LoadPolicyLoadPort {
    Optional<LoadPolicy> load();
}
