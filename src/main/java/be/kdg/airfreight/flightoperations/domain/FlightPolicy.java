package be.kdg.airfreight.flightoperations.domain;

public record FlightPolicy(int flightRetentionDays, int actualTimeToleranceMinutes) {
    private static final int DEFAULT_FLIGHT_RETENTION_DAYS = 7;
    private static final int DEFAULT_ACTUAL_TIME_TOLERANCE_MINUTES = 5;

    public FlightPolicy {
        if (flightRetentionDays < 1 || actualTimeToleranceMinutes < 0) {
            throw new IllegalArgumentException("Flight policy requires positive retention and non-negative actual-time tolerance");
        }
    }

    public static FlightPolicy defaults() {
        return new FlightPolicy(DEFAULT_FLIGHT_RETENTION_DAYS, DEFAULT_ACTUAL_TIME_TOLERANCE_MINUTES);
    }
}
