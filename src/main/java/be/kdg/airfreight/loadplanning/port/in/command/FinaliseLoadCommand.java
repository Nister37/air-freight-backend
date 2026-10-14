package be.kdg.airfreight.loadplanning.port.in.command;

import be.kdg.airfreight.common.FlightId;

public record FinaliseLoadCommand(FlightId flightId, boolean confirmWarnings) {

}
