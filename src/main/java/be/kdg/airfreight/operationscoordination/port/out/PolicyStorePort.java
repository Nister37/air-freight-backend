package be.kdg.airfreight.operationscoordination.port.out;

import be.kdg.airfreight.common.OperationsPolicy;
import java.util.Optional;

public interface PolicyStorePort {
    Optional<OperationsPolicy> load();
    void save(OperationsPolicy policy);
}
