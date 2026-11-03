package be.kdg.airfreight.loadplanning.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public record ULDCode(String value) {
    private static final Pattern FORMAT = Pattern.compile("[A-Z]{3}[0-9]{4,5}[A-Z0-9]{2}");

    public ULDCode{
        Objects.requireNonNull(value,"ULD code is required");
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid ULD code: " + value);
        }
    }
}
