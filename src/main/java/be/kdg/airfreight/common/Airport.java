package be.kdg.airfreight.common;

public record Airport(String value) {
    public Airport {
        if (value == null || !value.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("An IATA airport code must contain three uppercase letters");
        }
    }
}
