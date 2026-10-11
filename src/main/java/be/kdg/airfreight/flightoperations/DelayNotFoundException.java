package be.kdg.airfreight.flightoperations;

public class DelayNotFoundException extends RuntimeException {
    public DelayNotFoundException(String message) {
        super(message);
    }
}
