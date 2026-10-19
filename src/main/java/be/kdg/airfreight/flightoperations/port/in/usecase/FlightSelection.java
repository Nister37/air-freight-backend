package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.common.FlightStatus;
import java.time.LocalDate;

public record FlightSelection(LocalDate date, FlightNumber number, Airport departureAirport,
                               Airport arrivalAirport, FlightStatus status) { }
