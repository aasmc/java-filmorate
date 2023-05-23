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

}
