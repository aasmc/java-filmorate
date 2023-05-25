package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film save(Film film);

    Film update(Film film);

    List<Film> findAll();

    void addUserLikeToFilm(Long userId, Long filmId);

    void removeUserLike(Long userId, Long filmId);

    List<Film> getMostPopularFilms(long count);

    Optional<Film> findFilmById(Long filmId);

    Optional<Long> checkFilmId(Long id);

    default boolean filmAlreadyExists(Long filmId) {
        return filmId != null && checkFilmId(filmId).isPresent();
    }

    default boolean filmNotFound(Long filmId) {
        return filmId == null || checkFilmId(filmId).isEmpty();
    }

}
