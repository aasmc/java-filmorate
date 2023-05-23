package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.util.IdGenerator;

import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.storage.Constants.IN_MEMORY_GENRE_STORAGE;

@Component
@Qualifier(IN_MEMORY_GENRE_STORAGE)
@RequiredArgsConstructor
public class InMemoryGenreStorage implements GenreStorage {

    private final Map<Long, Genre> genreMap;
    private final IdGenerator idGenerator;

    @Override
    public List<Genre> findAll() {
        return List.copyOf(genreMap.values());
    }

    @Override
    public Genre getById(Long id) {
        if (genreNotFound(id)) {
            String msg = String.format("Genre with ID: %d is not found.", id);
            throw new ResourceNotFoundException(msg);
        }
        return genreMap.get(id);
    }

    @Override
    public Genre save(Genre genre) {
        if (!genreAlreadyExists(genre)) {
            genre.setId(idGenerator.nextId());
            genreMap.put(genre.getId(), genre);
        }
        return genre;
    }

    private boolean genreAlreadyExists(Genre genre) {
        return genre.getId() != null && genreMap.containsKey(genre.getId());
    }

    private boolean genreNotFound(Long id) {
        return !genreMap.containsKey(id);
    }
}
