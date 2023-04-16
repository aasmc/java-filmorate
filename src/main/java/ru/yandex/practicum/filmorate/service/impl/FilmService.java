package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.service.IFilmService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService implements IFilmService {

    private final FilmRepository filmRepository;


    @Override
    public List<Film> findAllFilms() {
        return filmRepository.findAll();
    }

    @Override
    public Film createFilm(Film film) {
        return filmRepository.save(film);
    }

    @Override
    public Film updateFilm(Film film) {
        return filmRepository.update(film);
    }
}
