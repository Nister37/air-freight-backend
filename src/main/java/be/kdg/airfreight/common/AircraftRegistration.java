package be.kdg.airfreight.common;

public record AircraftRegistration(String value) {
    public AircraftRegistration {
        if (value == null || !value.matches("[A-Z0-9][A-Z0-9-]{1,14}")) {
            throw new IllegalArgumentException("Invalid aircraft registration");
        }
    }
}
