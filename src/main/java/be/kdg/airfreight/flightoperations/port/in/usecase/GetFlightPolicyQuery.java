package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.flightoperations.domain.vo.FlightPolicy;

public interface GetFlightPolicyQuery {
    FlightPolicy currentPolicy();
}
