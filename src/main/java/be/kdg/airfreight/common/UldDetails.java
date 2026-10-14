package be.kdg.airfreight.common;

import java.math.BigDecimal;

public record UldDetails(String uldCode, BigDecimal weight,
                         String unit, Airport destination, String position) {

}