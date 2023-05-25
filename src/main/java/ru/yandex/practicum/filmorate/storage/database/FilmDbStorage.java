package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Constants;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.storage.Constants.DB_FILM_STORAGE;

@Component
@Qualifier(DB_FILM_STORAGE)
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private SimpleJdbcInsert filmsJdbcInsert;

    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         @Qualifier(Constants.DB_USER_STORAGE) UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        filmsJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("Films")
                .usingGeneratedKeyColumns("id");
        this.userStorage = userStorage;
    }

    @Override
    public Film save(Film film) {
        throwIfFilmExists(film.getId());
        Film saved = saveFilmInternal(film);
        saveFilmGenresInternal(film);
        saveFilmUserLikesInternal(film);
        return saved;
    }

    private void saveFilmGenresInternal(Film saved) {
        if (filmHasGenres(saved)) {
            String sql = "insert into FilmGenre (film_id, genre_id) values (?, ?)";
            jdbcTemplate.batchUpdate(
                    sql,
                    saved.getGenres(),
                    saved.getGenres().size(),
                    (PreparedStatement ps, Genre g) -> {
                        ps.setLong(1, saved.getId());
                        ps.setLong(2, g.getId());
                    }
            );
        }
    }

    private boolean filmHasGenres(Film saved) {
        return !saved.getGenres().isEmpty();
    }

    private void saveFilmUserLikesInternal(Film saved) {
        if (filmHasUserLikes(saved)) {
            String sql = "insert into FilmUserLikes (film_id, user_id) values (?, ?)";
            jdbcTemplate.batchUpdate(
                    sql,
                    saved.getUserLikes(),
                    saved.getUserLikes().size(),
                    (PreparedStatement ps, User u) -> {
                        ps.setLong(1, saved.getId());
                        ps.setLong(2, u.getId());
                    }
            );
        }
    }

    private boolean filmHasUserLikes(Film saved) {
        return !saved.getUserLikes().isEmpty();
    }

    private Film saveFilmInternal(Film film) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        if (film.getMpa().getId() != null) {
            parameters.put("rating_id", film.getName());
        }
        Long id = (Long) filmsJdbcInsert.executeAndReturnKey(parameters);
        film.setId(id);
        return film;
    }

    @Override
    public Film update(Film film) {
        throwIfFilmNotFound(film.getId());
        updateFilmInternal(film);
        deleteFilmGenresInternal(film);
        deleteFilmUserLikesInternal(film);
        saveFilmGenresInternal(film);
        saveFilmUserLikesInternal(film);
        return null;
    }

    private void updateFilmInternal(Film film) {
        String sql = "update Films set name = ?, description = ?, release_date = ?, " +
                "duration = ?, rating_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
    }

    private void deleteFilmUserLikesInternal(Film film) {
        String sql = "delete from FilmUserLikes where film_id = ?";
        jdbcTemplate.update(sql, film.getId());
    }

    private void deleteFilmGenresInternal(Film film) {
        String sql = "delete from FilmGenre where film_id = ?";
        jdbcTemplate.update(sql, film.getId());
    }

    @Override
    public List<Film> findAll() {
        return null;
    }

    @Override
    public void addUserLikeToFilm(Long userId, Long filmId) {
        throwIfFilmNotFound(filmId);
        throwIfUserNotFound(userId);

        String sql = "insert into FilmUserLikes (film_id, user_id) values (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeUserLike(Long userId, Long filmId) {
        throwIfUserNotFound(userId);
        throwIfFilmNotFound(filmId);

        String sql = "delete from FilmUserLikes where film_id = ? and user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(long count) {
        return null;
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        return null;
    }

    @Override
    public Optional<Long> checkFilmId(Long id) {
        String sql = "select id from Films where id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (rowSet.next()) {
            return Optional.of(rowSet.getLong("id"));
        }
        return Optional.empty();
    }

    private void throwIfFilmNotFound(Long filmId) {
        if (filmNotFound(filmId)) {
            String msg = String.format("Film with ID: %d is not found.", filmId);
            throw new ResourceNotFoundException(msg);
        }
    }

    private void throwIfFilmExists(Long filmId) {
        if (filmAlreadyExists(filmId)) {
            String msg = String.format("Film with ID: %d already exists", filmId);
            throw new ResourceAlreadyExistsException(msg);
        }
    }

    private void throwIfUserNotFound(Long userId) {
        if (userNotFound(userId)) {
            String msg = String.format("User with ID: %d is not found.", userId);
            throw new ResourceNotFoundException(msg);
        }
    }

    private boolean userNotFound(Long userId) {
        return userId == null || userStorage.checkUserId(userId).isEmpty();
    }
}
