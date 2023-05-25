package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.RatingStorage;
import ru.yandex.practicum.filmorate.util.IdGenerator;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.storage.Constants.*;

@Component
@Qualifier(IN_MEMORY_FILM_STORAGE)
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> filmMap;
    private final Map<Long, User> userMap;
    private final IdGenerator idGenerator;
    @Qualifier(IN_MEMORY_RATING_STORAGE)
    private final RatingStorage ratingStorage;
    @Qualifier(IN_MEMORY_GENRE_STORAGE)
    private final GenreStorage genreStorage;

    @Override
    public Film save(Film film) {
        if (filmAlreadyExists(film)) {
            String msg = String.format("Film with ID: %d already exists", film.getId());
            throw new ResourceAlreadyExistsException(msg);
        }
        film.setId(idGenerator.nextId());
        setGenresAndRatingToFilm(film);
        filmMap.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (filmNotFound(film)) {
            String msg = String.format("Film with ID: %d is not found.", film.getId());
            throw new ResourceNotFoundException(msg);
        }
        setGenresAndRatingToFilm(film);
        filmMap.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return List.copyOf(filmMap.values());
    }

    @Override
    public void addUserLikeToFilm(Long userId, Long filmId) {
        Film film = findFilmOrThrow(filmId);
        User user = findUserOrThrow(userId);
        film.addUserLike(user);
    }

    @Override
    public void removeUserLike(Long userId, Long filmId) {
        Film film = findFilmOrThrow(filmId);
        User user = findUserOrThrow(userId);
        film.removeUserLike(user);
    }

    @Override
    public List<Film> getMostPopularFilms(long count) {
        return filmMap.values().stream()
                .sorted(Comparator.comparing(Film::getLikesCount, Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        return Optional.ofNullable(filmMap.get(filmId));
    }

    private Film findFilmOrThrow(Long filmId) {
        return findFilmOrThrow(filmId,
                () -> provideNotFoundErrorMessage("Film", filmId));
    }

    private String provideNotFoundErrorMessage(String what, Long id) {
        return String.format("%s with ID=%d not found!", what, id);
    }

    private Film findFilmOrThrow(Long filmId, Supplier<String> msgSupplier) {
        return Optional.ofNullable(filmMap.get(filmId))
                .orElseThrow(() -> new ResourceNotFoundException(msgSupplier.get()));
    }

    private User findUserOrThrow(Long userId) {
        return findUserOrThrow(userId,
                () -> provideNotFoundErrorMessage("User", userId));
    }

    private User findUserOrThrow(Long userId, Supplier<String> msgSupplier) {
        return Optional.ofNullable(userMap.get(userId))
                .orElseThrow(() -> new ResourceNotFoundException(msgSupplier.get()));
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
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());
    }

    private Rating getFilmRating(Film film) {
        Rating rating = film.getMpa();
        if (rating == null) {
            return null;
        }
        return ratingStorage.getById(film.getMpa().getId())
                .orElseThrow(() -> {
                    String msg = String.format("Rating with ID: %d is not found.", film.getMpa().getId());
                    return new ResourceNotFoundException(msg);
                });
    }

    private boolean filmAlreadyExists(Film film) {
        return film.getId() != null && filmMap.containsKey(film.getId());
    }

    private boolean filmNotFound(Film film) {
        return film.getId() == null || !filmMap.containsKey(film.getId());
    }
}
