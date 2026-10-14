package be.kdg.airfreight.loadplanning;

public class LoadNotFoundException extends RuntimeException {
    public LoadNotFoundException(String message) {
        super(message);
    }
}
