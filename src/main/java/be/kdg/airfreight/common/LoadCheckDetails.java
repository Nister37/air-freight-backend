package be.kdg.airfreight.common;

public record LoadCheckDetails(String kind, String severity,
                               boolean passed, String explanation) {

}
