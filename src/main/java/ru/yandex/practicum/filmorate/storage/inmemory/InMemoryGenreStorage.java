package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.util.IdGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Optional<Genre> getById(Long id) {
        return Optional.ofNullable(genreMap.get(id));
    }

    @Override
    public Genre save(Genre genre) {
        if (genreAlreadyExists(genre)) {
            String msg = String.format("Genre already exists: %s", genre.getName().getGenre());
            throw new ResourceAlreadyExistsException(msg);
        }
        genre.setId(idGenerator.nextId());
        genreMap.put(genre.getId(), genre);
        return genre;
    }

    private boolean genreAlreadyExists(Genre genre) {
        return genre.getId() != null && genreMap.containsKey(genre.getId());
    }
}
