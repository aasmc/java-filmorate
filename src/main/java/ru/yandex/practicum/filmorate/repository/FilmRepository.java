package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmRepository {

    Film save(Film film);

    Film update(Film film);

    List<Film> findAll();

}