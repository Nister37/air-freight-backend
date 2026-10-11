package be.kdg.airfreight.operationscoordination.port.in.usecase;

import be.kdg.airfreight.common.*;
import java.time.*;
import java.util.*;

public interface UpdatePolicyUseCase {
    OperationsPolicy updatePolicy(OperationsPolicy policy);
}
