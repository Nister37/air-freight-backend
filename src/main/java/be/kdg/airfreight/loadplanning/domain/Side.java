package be.kdg.airfreight.loadplanning.domain;

public enum Side {
    LEFT("L"), RIGHT("R"), NOSE("N"), TAIL("T");

    private final String prefix;

    Side(String prefix) {
        this.prefix = prefix;
    }

    String prefix() {
        return prefix;
    }
}
