package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.IGenreService;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.storage.Constants.IN_MEMORY_GENRE_STORAGE;

@Service
@RequiredArgsConstructor
public class GenreService implements IGenreService {
    @Qualifier(IN_MEMORY_GENRE_STORAGE)
    private final GenreStorage genreStorage;

    @Override
    public Genre getGenreById(Long id) {
        return genreStorage.getById(id);
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
