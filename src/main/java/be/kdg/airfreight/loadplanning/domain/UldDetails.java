package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.common.Airport;

import java.math.BigDecimal;

public record UldDetails(String uldCode, BigDecimal weight,
                         String unit, Airport destination, String position) {
}
