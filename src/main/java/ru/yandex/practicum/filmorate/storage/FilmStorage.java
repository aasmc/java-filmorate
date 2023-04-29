package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film save(Film film);

    Film update(Film film);

    List<Film> findAll();

    void addUserLikeToFilm(Long userId, Long filmId);

    void removeUserLike(Long userId, Long filmId);

    List<Film> getMostPopularFilms(long count);

    Film findFilmById(Long filmId);

}
