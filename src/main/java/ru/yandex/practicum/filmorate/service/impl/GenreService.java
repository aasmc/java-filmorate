package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.IGenreService;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.storage.Constants.DB_GENRE_STORAGE;

@Service
@RequiredArgsConstructor
public class GenreService implements IGenreService {
    @Qualifier(DB_GENRE_STORAGE)
    private final GenreStorage genreStorage;

    @Override
    public Genre getGenreById(Long id) {
        return genreStorage.getById(id)
                .orElseThrow(() -> {
                    String msg = String.format("Genre with ID: %d is not found.", id);
                    return new ResourceNotFoundException(msg);
                });
    }

    @Override
    public List<Genre> getAllGenres() {
        return genreStorage.findAll();
    }

    @Override
    public Genre addGenre(Genre genre) {
        return genreStorage.save(genre);
    }
}
