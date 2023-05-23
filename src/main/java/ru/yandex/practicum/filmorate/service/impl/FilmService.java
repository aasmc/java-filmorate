package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.IFilmService;
import ru.yandex.practicum.filmorate.service.IUserService;
import ru.yandex.practicum.filmorate.storage.Constants;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService implements IFilmService {

    @Qualifier(Constants.IN_MEMORY_FILM_STORAGE)
    private final FilmStorage filmStorage;
    private final IUserService userService;
    @Qualifier(Constants.IN_MEMORY_RATING_STORAGE)
    private final RatingStorage ratingStorage;
    @Qualifier(Constants.IN_MEMORY_GENRE_STORAGE)
    private final GenreStorage genreStorage;

    @Override
    public List<Film> findAllFilms() {
        return filmStorage.findAll();
    }

    @Override
    public Film createFilm(Film film) {
        setGenresAndRatingToFilm(film);
        return filmStorage.save(film);
    }

    @Override
    public Film updateFilm(Film film) {
        setGenresAndRatingToFilm(film);
        return filmStorage.update(film);
    }

    private void setGenresAndRatingToFilm(Film film) {
        List<Genre> genres = getFilmGenres(film);
        film.setGenres(genres);
        Rating rating = getFilmRating(film);
        film.setMpa(rating);
    }

    private List<Genre> getFilmGenres(Film film) {
        return film.getGenres().stream()
                .map(Genre::getId)
                .map(genreStorage::getById)
                .distinct()
                .collect(Collectors.toList());
    }

    private Rating getFilmRating(Film film) {
        Rating rating = film.getMpa();
        if (rating == null) {
            return null;
        }
        return ratingStorage.getById(film.getMpa().getId());
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
        return filmStorage.findFilmById(filmId);
    }

    private User getUser(Long userId) {
        return userService.getUserById(userId);
    }
}
