package be.kdg.airfreight.loadplanning.port.in.command;

import be.kdg.airfreight.common.FlightId;

public record PlaceULDCommand(FlightId flightId, String uldCode, String position) {

}
