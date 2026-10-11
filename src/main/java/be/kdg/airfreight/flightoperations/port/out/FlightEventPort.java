package be.kdg.airfreight.flightoperations.port.out;

import be.kdg.airfreight.common.events.DomainEvent;

public interface FlightEventPort { void publish(DomainEvent event); }
