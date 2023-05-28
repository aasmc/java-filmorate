package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.GenreName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreDbStorage;

    @Test
    void getAllGenres_returnsCorrectListOfGenres() {
        List<Genre> all = genreDbStorage.findAll();
        List<Genre> expected = List.of(
                Genre.builder().id(1L).name(GenreName.COMEDY).build(),
                Genre.builder().id(2L).name(GenreName.DRAMA).build(),
                Genre.builder().id(3L).name(GenreName.CARTOON).build(),
                Genre.builder().id(4L).name(GenreName.THRILLER).build(),
                Genre.builder().id(5L).name(GenreName.DOCUMENTARY).build(),
                Genre.builder().id(6L).name(GenreName.ACTION).build()
        );
        assertThat(all).isEqualTo(expected);
    }

    @Test
    void getGenreById_returnsGenre() {
        Optional<Genre> byId = genreDbStorage.getById(1L);
        assertThat(byId).isPresent();
        Genre genre = byId.get();
        Genre expected = Genre.builder().id(1L).name(GenreName.COMEDY).build();
        assertThat(genre).isEqualTo(expected);
    }

    @Test
    void getGenreById_returnsEmptyOptionalIfIdNotFoundInDb() {
        Optional<Genre> byId = genreDbStorage.getById(10L);
        assertThat(byId).isEmpty();
    }

}