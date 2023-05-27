package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.Constants;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.dbutils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.storage.Constants.DB_FILM_STORAGE;

@Component
@Qualifier(DB_FILM_STORAGE)
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ColumnNamesProvider columnNamesProvider;
    private final SqlProvider sqlProvider;
    private final UserStorage userStorage;

    private SimpleJdbcInsert filmsJdbcInsert;

    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         ColumnNamesProvider columnNamesProvider,
                         SqlProvider sqlProvider,
                         @Qualifier(Constants.DB_USER_STORAGE) UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        filmsJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("Films")
                .usingGeneratedKeyColumns("id");
        this.columnNamesProvider = columnNamesProvider;
        this.userStorage = userStorage;
        this.sqlProvider = sqlProvider;
    }

    @Override
    public Film save(Film film) {
        throwIfFilmExists(film.getId());
        Film saved = saveFilmInternal(film);
        saveFilmGenresInternal(film);
        saveFilmUserLikesInternal(film);
        // return film with all info about it
        return findFilmById(saved.getId()).get();
    }

    @Override
    public Film update(Film film) {
        throwIfFilmNotFound(film.getId());
        deleteFilmGenresInternal(film);
        deleteFilmUserLikesInternal(film);
        updateFilmInternal(film);
        saveFilmGenresInternal(film);
        saveFilmUserLikesInternal(film);
        return findFilmById(film.getId()).get();
    }

    @Override
    public List<Film> findAll() {
        String sql = sqlProvider.provideFilmFindAllSql();
        return selectFilmsBySqlInternal(sql);
    }


    @Override
    public void addUserLikeToFilm(Long userId, Long filmId) {
        throwIfFilmNotFound(filmId);
        throwIfUserNotFound(userId);

        String sql = sqlProvider.provideAddUserLikesToFilmSql();
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeUserLike(Long userId, Long filmId) {
        throwIfUserNotFound(userId);
        throwIfFilmNotFound(filmId);

        String sql = sqlProvider.provideRemoveSingleUserLikeForFilmSql();
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(long count) {
        String sql = sqlProvider.provideMostPopularFilmsSql();
        return selectFilmsBySqlInternal(sql, count);
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        String sql = sqlProvider.provideFindFilmByIdSql();

        Film film = jdbcTemplate.query(sql, rs -> {
            Film extractedFilm = null;
            Set<Long> userIds = new HashSet<>();
            Map<Long, Genre> idToGenre = new HashMap<>();
            Rating rating = null;
            while (rs.next()) {
                if (rating == null) {
                    rating = extractRating(rs, columnNamesProvider.provideRatingColumns());
                }
                GenreColumns genreColumns = columnNamesProvider.provideGenreColumns();
                extractGenreAndSaveToMap(rs, idToGenre, genreColumns);

                UserColumns columns = columnNamesProvider.provideUserColumns();
                extractUserIdAndSaveToSet(rs, userIds, columns);

                FilmColumns filmColumns = columnNamesProvider.provideFilmColumns();
                if (extractedFilm == null) {
                    extractedFilm = extractFilm(rs, filmColumns, filmId);
                }
            }

            if (extractedFilm != null) {
                List<Genre> genres = getUniqueGenresSortedById(idToGenre);
                extractedFilm.setGenres(genres);
                extractedFilm.setUserLikes(userIds);
                if (rating != null) {
                    extractedFilm.setMpa(rating);
                }
            }
            return extractedFilm;
        }, filmId);

        return Optional.ofNullable(film);
    }

    @Override
    public Optional<Long> checkFilmId(Long id) {
        String sql = sqlProvider.provideCheckFilmIdSql();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (rowSet.next()) {
            return Optional.of(rowSet.getLong("id"));
        }
        return Optional.empty();
    }

    private List<Genre> getUniqueGenresSortedById(Map<Long, Genre> idToGenre) {
        return idToGenre.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparingLong(Genre::getId))
                .collect(Collectors.toList());
    }

    private Film extractFilm(ResultSet rs, FilmColumns columns, Long id) throws SQLException {
        String filmName = rs.getString(columns.getName());
        String description = rs.getString(columns.getDescr());
        LocalDate releaseDate = rs.getDate(columns.getReleaseDate()).toLocalDate();
        long duration = rs.getLong(columns.getDuration());
        return Film.builder()
                .id(id)
                .name(filmName)
                .description(description)
                .releaseDate(releaseDate)
                .duration(duration)
                .build();
    }


    private Rating extractRating(ResultSet rs, RatingColumns columns) throws SQLException {
        Rating rating = null;
        Long mpaId = rs.getLong(columns.getId());
        if (mpaId != 0) {
            String mpa = rs.getString(columns.getName());
            rating = Rating.builder()
                    .id(mpaId)
                    .name(RatingName.fromString(mpa))
                    .build();
        }
        return rating;
    }

    private void extractUserIdAndSaveToSet(ResultSet rs,
                                           Set<Long> userIds,
                                           UserColumns columns) throws SQLException {
        Long userId = rs.getLong(columns.getId());
        if (userId != 0) {
            userIds.add(userId);
        }
    }

    private Genre extractGenreAndSaveToMap(ResultSet rs,
                                           Map<Long, Genre> idToGenre,
                                           GenreColumns columns) throws SQLException {
        Genre genre = null;
        Long genreId = rs.getLong(columns.getId());
        if (genreId != 0) {
            genre = idToGenre.get(genreId);
            if (genre == null) {
                String genreName = rs.getString(columns.getName());
                genre = Genre.builder()
                        .id(genreId)
                        .name(GenreName.fromString(genreName))
                        .build();
                idToGenre.put(genreId, genre);
            }
        }
        return genre;
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

    private void updateFilmInternal(Film film) {
        String sql = sqlProvider.provideUpdateFilmSql();
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
    }

    private void saveFilmGenresInternal(Film saved) {
        if (filmHasGenres(saved)) {
            List<Genre> genres = saved.getGenres().stream()
                    .distinct()
                    .collect(Collectors.toList());
            String sql = sqlProvider.provideSaveFilmGenresSql();
            jdbcTemplate.batchUpdate(
                    sql,
                    genres,
                    genres.size(),
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
            String sql = sqlProvider.provideAddUserLikesToFilmSql();
            jdbcTemplate.batchUpdate(
                    sql,
                    saved.getUserLikes(),
                    saved.getUserLikes().size(),
                    (PreparedStatement ps, Long uId) -> {
                        ps.setLong(1, saved.getId());
                        ps.setLong(2, uId);
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
            parameters.put("rating_id", film.getMpa().getId());
        }
        Long id = (Long) filmsJdbcInsert.executeAndReturnKey(parameters);
        film.setId(id);
        return film;
    }

    private void deleteFilmUserLikesInternal(Film film) {
        String sql = sqlProvider.provideRemoveAllUserLikesForFilmSql();
        jdbcTemplate.update(sql, film.getId());
    }

    private void deleteFilmGenresInternal(Film film) {
        String sql = sqlProvider.provideDeleteFilmGenresSql();
        jdbcTemplate.update(sql, film.getId());
    }

    private List<Film> selectFilmsBySqlInternal(String sql, Object... args) {
        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Film> idToFilm = new HashMap<>();
            Map<Long, Genre> idToGenre = new HashMap<>();
            while (rs.next()) {
                Rating rating = extractRating(rs, columnNamesProvider.provideRatingColumns());

                GenreColumns genreColumns = columnNamesProvider.provideGenreColumns();
                Genre genre = extractGenreAndSaveToMap(rs, idToGenre, genreColumns);

                UserColumns userColumns = columnNamesProvider.provideUserColumns();
                Long userId = extractUserId(rs, userColumns);

                FilmColumns filmColumns = columnNamesProvider.provideFilmColumns();
                Long filmId = rs.getLong(filmColumns.getId());
                Film film = idToFilm.get(filmId);
                if (film == null) {
                    film = extractFilm(rs, filmColumns, filmId);
                    // set rating only once since it is one-to-one
                    if (rating != null) {
                        film.setMpa(rating);
                    }
                    idToFilm.put(filmId, film);
                }
                if (genre != null) {
                    film.getGenres().add(genre);
                }
                if (userId != null) {
                    film.addUserLike(userId);
                }
            }
            return List.copyOf(idToFilm.values());
        }, args);
    }

    private Long extractUserId(ResultSet rs, UserColumns userColumns) throws SQLException {
        Long userId = rs.getLong(userColumns.getId());
        return userId != 0 ? userId : null;
    }
}
