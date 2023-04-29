package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Film>> getAllFilms() {
        List<Film> films = service.findAllFilms();
        return new ResponseEntity<>(films, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@RequestBody @Valid Film film) {
        log.debug("Creating film {}", film);
        Film created = service.createFilm(film);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@RequestBody @Valid Film film) {
        log.debug("Updating film {}", film);
        Film updated = service.updateFilm(film);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable("id") Long filmId,
                        @PathVariable("userId") Long userId) {
        service.addLikeByUserWithId(userId, filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLike(@PathVariable("id") Long filmId,
                           @PathVariable("userId") Long userId) {
        service.removeLikeByUserWithId(userId, filmId);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopularFilms(@RequestParam(name = "count", defaultValue = "10") Integer count) {
        List<Film> films = service.getMostPopularFilms(count);
        return new ResponseEntity<>(films, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable("id") Long id) {
        Film film = service.getFilmById(id);
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

}
