package ru.yandex.practicum.filmorate.repository.inmemory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.util.IdGenerator;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class InMemoryFilmRepository implements FilmRepository {

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

    private boolean filmAlreadyExists(Film film) {
        return film.getId() != null && filmMap.containsKey(film.getId());
    }

    private boolean filmNotFound(Film film) {
        return film.getId() == null || !filmMap.containsKey(film.getId());
    }
}
