package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.practicum.filmorate.testutil.TestConstants.LONG_DESCRIPTION;
import static ru.yandex.practicum.filmorate.testutil.TestConstants.THRESHOLD_DATE;

public class FilmValidationTests extends BaseValidationTest {

    @Test
    void whenValidFilm_ConstraintViolationsEmpty() {
        Film film = Film.builder()
                .name("Name")
                .description("Description")
                .releaseDate(LocalDate.now())
                .duration(10)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidFilms")
    void whenInvalidFilm_ConstraintViolationsNotEmpty(Film film) {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).isNotEmpty();
    }

    private static Stream<Film> invalidFilms() {
        return Stream.of(
                Film // null name
                        .builder()
                        .description("Description")
                        .releaseDate(LocalDate.now())
                        .duration(10)
                        .build(),
                Film // long description
                        .builder()
                        .name("Name")
                        .description(LONG_DESCRIPTION)
                        .releaseDate(LocalDate.now())
                        .duration(10)
                        .build(),
                Film // negative duration
                        .builder()
                        .name("Name")
                        .description("Description")
                        .releaseDate(LocalDate.now())
                        .duration(-10)
                        .build(),
                Film // invalid release date
                        .builder()
                        .name("Name")
                        .description("Description")
                        .releaseDate(THRESHOLD_DATE)
                        .duration(10)
                        .build(),
                Film // all fields incorrect
                        .builder()
                        .name("")
                        .description(LONG_DESCRIPTION)
                        .releaseDate(THRESHOLD_DATE)
                        .duration(-10)
                        .build()
        );
    }

}
