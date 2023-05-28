package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.testutil.TestConstants;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.filmorate.testutil.TestDataProvider.*;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(
        scripts = "classpath:db/clean-films.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Test
    void whenFindFilmById_ifFilmExistsInDb_returnsNonEmptyOptional() {
        Film film = newFilm();
        film = filmDbStorage.save(film);

        Optional<Film> filmById = filmDbStorage.findFilmById(film.getId());
        assertThat(filmById).isNotEmpty();
        Film retrieved = filmById.get();
        assertThat(retrieved).isEqualTo(film);
    }

    @Test
    void whenFindNonExistingFilmById_returnsEmptyOptional() {
        Optional<Film> filmById = filmDbStorage.findFilmById(100L);
        assertThat(filmById).isEmpty();
    }

    @Sql(
            scripts = "classpath:db/film-data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    void whenGetMostPopularFilmsIfSomeFilmsContainUserLikes_returnsCountFilmsWithUserLikesSortedByLikes() {
        User user = newUser();
        user = userDbStorage.save(user);
        User user2 = newUser();
        user2 = userDbStorage.save(user2);
        User user3 = newUser();
        user3 = userDbStorage.save(user3);
        User user4 = newUser();
        user4 = userDbStorage.save(user4);

        Film film = newFilm();
        film = filmDbStorage.save(film);
        filmDbStorage.addUserLikeToFilm(user.getId(), film.getId());
        filmDbStorage.addUserLikeToFilm(user2.getId(), film.getId());
        filmDbStorage.addUserLikeToFilm(user3.getId(), film.getId());
        filmDbStorage.addUserLikeToFilm(user4.getId(), film.getId());
        film = filmDbStorage.findFilmById(film.getId()).get();

        Film film2 = newFilm();
        film2 = filmDbStorage.save(film2);
        filmDbStorage.addUserLikeToFilm(user.getId(), film2.getId());
        filmDbStorage.addUserLikeToFilm(user2.getId(), film2.getId());
        filmDbStorage.addUserLikeToFilm(user3.getId(), film2.getId());
        film2 = filmDbStorage.findFilmById(film2.getId()).get();

        Film film3 = newFilm();
        film3 = filmDbStorage.save(film3);
        filmDbStorage.addUserLikeToFilm(user.getId(), film3.getId());
        filmDbStorage.addUserLikeToFilm(user2.getId(), film3.getId());
        film3 = filmDbStorage.findFilmById(film3.getId()).get();

        Film film4 = newFilm();
        film4 = filmDbStorage.save(film4);
        filmDbStorage.addUserLikeToFilm(user.getId(), film4.getId());

        List<Film> popular = filmDbStorage.getMostPopularFilms(3);
        assertThat(popular).containsExactly(film, film2, film3);
    }

    @Sql(
            scripts = "classpath:db/film-data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    void whenGetMostPopularFilmsIfAllFilmsWithoutUserLikes_returnsFilms() {
        List<Film> popular = filmDbStorage.getMostPopularFilms(10);
        assertThat(popular).containsExactlyInAnyOrder(filmOne(), filmTwo(), filmThree());
    }

    @Test
    void whenGetMostPopularFilmsOnEmptyDb_returnsEmptyList() {
        List<Film> popular = filmDbStorage.getMostPopularFilms(10);
        assertThat(popular).isEmpty();
    }

    @Test
    void whenRemoveUserLikeFromFilmWithUserLike_queryingForFilmReturnsFilmWithoutThatUserLike() {
        User user = newUser();
        user = userDbStorage.save(user);
        Film film = newFilm();
        film = filmDbStorage.save(film);

        filmDbStorage.addUserLikeToFilm(user.getId(), film.getId());
        filmDbStorage.removeUserLike(user.getId(), film.getId());

        Film retrieved = filmDbStorage.findFilmById(film.getId()).get();
        assertThat(retrieved.getUserLikes()).isEmpty();
    }

    @Test
    void whenRemoveUserLikeFromFilmWithNoUserLikes_nothingHappens() {
        User user = newUser();
        user = userDbStorage.save(user);
        Film film = newFilm();
        film = filmDbStorage.save(film);
        boolean allIsOk = true;
        filmDbStorage.removeUserLike(user.getId(), film.getId());
        assertTrue(allIsOk);
    }

    @Test
    void whenRemoveUserLikeFromFilmByNonExistingUser_throws() {
        Film film = newFilm();
        Film saved = filmDbStorage.save(film);

        assertThrows(
                ResourceNotFoundException.class,
                () -> filmDbStorage.removeUserLike(100L, saved.getId())
        );
    }

    @Test
    void whenRemoveUserLikeFromNonExistingFilm_throws() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> filmDbStorage.removeUserLike(100L, 101L)
        );
    }

    @Test
    void whenAddUserLikeToFilm_queryingForFilm_returnsItWithUserLikeInfo() {
        User user = newUser();
        user = userDbStorage.save(user);
        Film film = newFilm();
        film = filmDbStorage.save(film);

        filmDbStorage.addUserLikeToFilm(user.getId(), film.getId());

        Film retrieved = filmDbStorage.findFilmById(film.getId()).get();
        assertThat(retrieved.getUserLikes()).containsExactly(user.getId());
    }

    @Test
    void whenAddUserLikeToFilmByNonExistingUser_throws() {
        Film film = newFilm();
        Film saved = filmDbStorage.save(film);

        assertThrows(
                ResourceNotFoundException.class,
                () -> filmDbStorage.addUserLikeToFilm(100L, saved.getId())
        );
    }

    @Test
    void whenAddUserLikeToNonExistingFilm_throws() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> filmDbStorage.addUserLikeToFilm(100L, 101L)
        );
    }

    @Sql(
            scripts = "classpath:db/film-data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    void whenFindAllOnNonEmptyDb_returnsListOfFilms() {
        List<Film> all = filmDbStorage.findAll();
        assertThat(all).containsExactly(filmOne(), filmTwo(), filmThree());
    }

    @Test
    void whenFindAllOnEmptyDb_returnsEmptyList() {
        List<Film> all = filmDbStorage.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void whenUpdateExistingFilmsWithNonUniqueGenres_returnsUpdatedFilmWithUniqueGenres() {
        Film film = newFilm();
        film = filmDbStorage.save(film);

        Film toUpdate = film.setMpa(Rating.builder().id(3L).name(RatingName.PG_13).build())
                .setName("Updated Name")
                .setDescription("Updated Description")
                .setGenres(List.of(
                        Genre.builder().id(1L).name(GenreName.COMEDY).build(),
                        Genre.builder().id(2L).name(GenreName.DRAMA).build(),
                        Genre.builder().id(1L).name(GenreName.COMEDY).build()
                ));

        Film updated = filmDbStorage.update(toUpdate);
        assertThat(updated.getId()).isEqualTo(film.getId());
        assertThat(updated.getName()).isEqualTo(toUpdate.getName());
        assertThat(updated.getDescription()).isEqualTo(toUpdate.getDescription());
        assertThat(updated.getMpa()).isEqualTo(toUpdate.getMpa());
        assertThat(updated.getGenres()).isNotEqualTo(toUpdate.getGenres());
        assertThat(updated.getGenres()).isEqualTo(List.of(
                Genre.builder().id(1L).name(GenreName.COMEDY).build(),
                Genre.builder().id(2L).name(GenreName.DRAMA).build()
        ));
    }

    @Test
    void whenUpdateExistingFilm_ifNewFilmHasNullAsMpa_returnsUpdatedFilm() {
        Film film = newFilm();
        film = filmDbStorage.save(film);

        Film toUpdate = film.setMpa(null)
                .setName("Updated Name")
                .setDescription("Updated Description")
                .setGenres(List.of(
                        Genre.builder().id(1L).name(GenreName.COMEDY).build(),
                        Genre.builder().id(2L).name(GenreName.DRAMA).build(),
                        Genre.builder().id(3L).name(GenreName.CARTOON).build()
                ));

        Film updated = filmDbStorage.update(toUpdate);
        assertThat(updated.getId()).isEqualTo(film.getId());
        assertThat(updated.getName()).isEqualTo(toUpdate.getName());
        assertThat(updated.getDescription()).isEqualTo(toUpdate.getDescription());
        assertThat(updated.getMpa()).isNull();
        assertThat(updated.getGenres()).isEqualTo(toUpdate.getGenres());
    }

    @Test
    void whenUpdateExistingFilm_returnsUpdatedFilm() {
        Film film = newFilm();
        film = filmDbStorage.save(film);

        Film toUpdate = film.setMpa(Rating.builder().id(3L).name(RatingName.PG_13).build())
                .setName("Updated Name")
                .setDescription("Updated Description")
                .setGenres(List.of(
                        Genre.builder().id(1L).name(GenreName.COMEDY).build(),
                        Genre.builder().id(2L).name(GenreName.DRAMA).build(),
                        Genre.builder().id(3L).name(GenreName.CARTOON).build()
                ));

        Film updated = filmDbStorage.update(toUpdate);
        assertThat(updated.getId()).isEqualTo(film.getId());
        assertThat(updated.getName()).isEqualTo(toUpdate.getName());
        assertThat(updated.getDescription()).isEqualTo(toUpdate.getDescription());
        assertThat(updated.getMpa()).isEqualTo(toUpdate.getMpa());
        assertThat(updated.getGenres()).isEqualTo(toUpdate.getGenres());
    }

    @Test
    void whenUpdateNonExistingFilm_throws() {
        Film film = newFilm().setId(100L);
        assertThrows(
                ResourceNotFoundException.class,
                () -> filmDbStorage.update(film)
        );
    }

    @Test
    void whenSaveFilmThatExistsInDb_throws() {
        Film film = newFilm();
        film = filmDbStorage.save(film);

        Film newFilm = newFilm();
        newFilm.setId(film.getId());
        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> filmDbStorage.save(newFilm)
        );
    }

    @Test
    void whenSaveNonExistingFilm_savedFilmAndReturnsItWithId() {
        Film film = newFilm();
        Film retrieved = filmDbStorage.save(film);
        assertThat(retrieved.getId()).isNotNull();
        assertThat(retrieved.getName()).isEqualTo(TestConstants.NEW_FILM_NAME);
        assertThat(retrieved.getDescription()).isEqualTo(TestConstants.NEW_FILM_DESCR);
        assertThat(retrieved.getReleaseDate()).isEqualTo(TestConstants.NEW_FILM_REL_D);
        assertThat(retrieved.getDuration()).isEqualTo(TestConstants.NEW_FILM_DUR);
        assertThat(retrieved.getMpa()).isEqualTo(TestConstants.NEW_FILM_RATING);
    }

}