package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.IFilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final IFilmService service;

    @GetMapping
    public List<Film> getAllFilms() {
        return service.findAllFilms();
    }

    @PostMapping
    public Film createFilm(@RequestBody @Valid Film film) {
        log.debug("Creating film {}", film);
        return service.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film) {
        log.debug("Updating film {}", film);
        return service.updateFilm(film);
    }
}
