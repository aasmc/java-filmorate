package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RatingName {
    G("G"),
    PG("PG"),
    PG_13("PG-13"),
    R("R"),
    NC_17("NC-17");

    @JsonValue
    private final String rating;

    RatingName(String rating) {
        this.rating = rating;
    }

    public static RatingName fromString(String str) {
        switch (str) {
            case "G":
                return G;
            case "PG":
                return PG;
            case "PG-13":
                return PG_13;
            case "R":
                return R;
            case "NC-17":
                return NC_17;
            default:
                String msg = String.format("Unknown rating: '%s'.", str);
                throw new IllegalArgumentException(msg);
        }
    }
}
