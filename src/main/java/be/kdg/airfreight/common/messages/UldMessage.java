package be.kdg.airfreight.common.messages;

import java.math.BigDecimal;
import java.util.UUID;

public record UldMessage(UUID messageId, String flightNumber, int departureDay,
                         String departureAirport, String uldCode, BigDecimal weight, String unit,
                         String destination) { }
