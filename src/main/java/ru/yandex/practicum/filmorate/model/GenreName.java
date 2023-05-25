package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GenreName {
    DRAMA("Драма"),
    COMEDY("Комедия"),
    THRILLER("Триллер"),
    ACTION("Боевик"),
    DOCUMENTARY("Документальный"),
    CARTOON("Мультфильм");

    @JsonValue
    private final String genre;

    GenreName(String genre) {
        this.genre = genre;
    }

    public static GenreName fromString(String str) {
        switch (str) {
            case "Драма":
                return DRAMA;
            case "Комедия":
                return COMEDY;
            case "Триллер":
                return THRILLER;
            case "Боевик":
                return ACTION;
            case "Документальный":
                return DOCUMENTARY;
            case "Мультфильм":
                return CARTOON;
            default:
                String msg = String.format("Unknown Genre: '%s'.", str);
                throw new IllegalArgumentException(msg);
        }
    }
}
