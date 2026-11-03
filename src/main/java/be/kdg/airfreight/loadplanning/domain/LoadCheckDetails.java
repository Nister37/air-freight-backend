package be.kdg.airfreight.loadplanning.domain;

public record LoadCheckDetails(String kind, String severity,
                               boolean passed, String explanation) {

}
