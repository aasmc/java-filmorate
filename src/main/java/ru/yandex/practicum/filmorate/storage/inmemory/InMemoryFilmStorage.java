package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.util.IdGenerator;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> filmMap;
    private final IdGenerator idGenerator;

    @Override
    public Film save(Film film) {
        if (filmAlreadyExists(film)) {
            String msg = String.format("Film with ID: %d already exists", film.getId());
            throw new ResourceAlreadyExistsException(msg);
        }
        film.setId(idGenerator.nextId());
        filmMap.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (filmNotFound(film)) {
            String msg = String.format("Film with ID: %d is not found.", film.getId());
            throw new ResourceNotFoundException(msg);
        }
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
        film.addLikeByUserWithId(userId);
    }

    @Override
    public void removeUserLike(Long userId, Long filmId) {
        Film film = findFilmOrThrow(filmId);
        film.removeLikeByUserWithId(userId);
    }

    @Override
    public List<Film> getMostPopularFilms(long count) {
        return filmMap.values().stream()
                .sorted(Comparator.comparing(Film::getLikesCount, Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Film findFilmById(Long filmId) {
        return findFilmOrThrow(filmId);
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

    private boolean filmAlreadyExists(Film film) {
        return film.getId() != null && filmMap.containsKey(film.getId());
    }

    private boolean filmNotFound(Film film) {
        return film.getId() == null || !filmMap.containsKey(film.getId());
    }
}
