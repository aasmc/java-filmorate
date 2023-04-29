package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface IFilmService {

    List<Film> findAllFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    void addLikeByUserWithId(Long userId, Long filmId);

    void removeLikeByUserWithId(Long userId, Long filmId);

    List<Film> getMostPopularFilms(long count);

    Film getFilmById(Long filmId);

}
