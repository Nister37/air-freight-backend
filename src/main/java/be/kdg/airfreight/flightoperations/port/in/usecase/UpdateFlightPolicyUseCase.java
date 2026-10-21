package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.flightoperations.domain.FlightPolicy;
import be.kdg.airfreight.flightoperations.port.in.command.UpdateFlightPolicyCommand;

public interface UpdateFlightPolicyUseCase {
    FlightPolicy updatePolicy(UpdateFlightPolicyCommand command);
}
