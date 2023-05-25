package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.storage.Constants.DB_FILM_STORAGE;

@Component
@Qualifier(DB_FILM_STORAGE)
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    @Override
    public Film save(Film film) {
        return null;
    }

    @Override
    public Film update(Film film) {
        return null;
    }

    @Override
    public List<Film> findAll() {
        return null;
    }

    @Override
    public void addUserLikeToFilm(Long userId, Long filmId) {

    }

    @Override
    public void removeUserLike(Long userId, Long filmId) {

    }

    @Override
    public List<Film> getMostPopularFilms(long count) {
        return null;
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        return null;
    }
}
