package be.kdg.airfreight.common;

public record FlightNumber(String value) {
    public FlightNumber {
        if (value == null || !value.matches("[A-Z0-9]{2}[0-9]{1,4}[A-Z]?")) {
            throw new IllegalArgumentException("Flight number must contain an airline code and flight number");
        }
    }
}
