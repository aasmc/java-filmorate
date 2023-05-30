package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.IFilmService;
import ru.yandex.practicum.filmorate.service.IUserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService implements IFilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    private final IUserService userService;

    @Override
    public List<Film> findAllFilms() {
        return filmStorage.findAll();
    }

    @Override
    public Film createFilm(Film film) {
        return filmStorage.save(film);
    }

    @Override
    public Film updateFilm(Film film) {
        return filmStorage.update(film);
    }

    @Override
    public void addLikeByUserWithId(Long userId, Long filmId) {
        // ensures that we have the user in storage
        User user = getUser(userId);
        filmStorage.addUserLikeToFilm(user.getId(), filmId);
    }

    @Override
    public void removeLikeByUserWithId(Long userId, Long filmId) {
        // ensures that we have the user in storage
        User user = getUser(userId);
        filmStorage.removeUserLike(user.getId(), filmId);
    }

    @Override
    public List<Film> getMostPopularFilms(long count) {
        return filmStorage.getMostPopularFilms(count);
    }

    @Override
    public Film getFilmById(Long filmId) {
        return filmStorage.findFilmById(filmId)
                .orElseThrow(() -> {
                    String msg = String.format("Film with ID = %d not found.", filmId);
                    return new ResourceNotFoundException(msg);
                });
    }

    private User getUser(Long userId) {
        return userService.getUserById(userId);
    }
}
