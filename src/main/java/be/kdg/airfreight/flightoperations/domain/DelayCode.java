package be.kdg.airfreight.flightoperations.domain;

public record DelayCode(String value) {
    public DelayCode {
        if (value == null || !value.matches("[0-9]{2}")) {
            throw new IllegalArgumentException("Delay code must contain two digits");
        }
    }
}
