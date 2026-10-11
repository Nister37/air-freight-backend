package be.kdg.airfreight.common;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record CargoBooking(UUID messageId, FlightId flightId, String uldCode,
                           BigDecimal weight, String unit, Airport destination) {
    public CargoBooking {
        Objects.requireNonNull(messageId);
        Objects.requireNonNull(flightId);
        if (uldCode == null || !uldCode.matches("[A-Z]{3}[0-9]{4,5}[A-Z0-9]{2}")) throw new IllegalArgumentException("Invalid ULD code");
        if (weight == null || weight.signum() <= 0) throw new IllegalArgumentException("ULD weight must be positive");
        if (!"KG".equals(unit) && !"LB".equals(unit)) throw new IllegalArgumentException("Weight unit must be KG or LB");
        Objects.requireNonNull(destination);
    }
}
