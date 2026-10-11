package be.kdg.airfreight.operationscoordination;

public class LoadPlanningUnavailableException extends RuntimeException {
    public LoadPlanningUnavailableException() { super("Load Planning is not installed yet; the booking cannot be applied"); }
}
