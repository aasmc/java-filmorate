package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface IGenreService {

    Genre getGenreById(Long id);

    List<Genre> getAllGenres();

    Genre addGenre(Genre genre);

}
