package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.RatingStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.dbutils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ColumnNamesProvider columnNamesProvider;
    private final SqlProvider sqlProvider;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Qualifier("genreDbStorage")
    private final GenreStorage genreDbStorage;
    @Qualifier("ratingDbStorage")
    private final RatingStorage ratingDbStorage;

    @Transactional
    @Override
    public Film save(Film film) {
        throwIfFilmExists(film.getId());
        Film saved = saveFilmInternal(film);
        saveFilmGenresInternal(film);
        saveFilmUserLikesInternal(film);
        // return film with all info about it
        return findFilmById(saved.getId()).get();
    }

    @Transactional
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
                GenreTable genreTable = columnNamesProvider.provideGenreColumns();
                extractGenreAndSaveToMap(rs, idToGenre, genreTable);

                UserTable columns = columnNamesProvider.provideUserColumns();
                extractUserIdAndSaveToSet(rs, userIds, columns);

                FilmTable filmTable = columnNamesProvider.provideFilmColumns();
                if (extractedFilm == null) {
                    extractedFilm = extractFilm(rs, filmTable, filmId);
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

    private Film extractFilm(ResultSet rs, FilmTable columns, Long id) throws SQLException {
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


    private Rating extractRating(ResultSet rs, RatingTable columns) throws SQLException {
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
                                           UserTable columns) throws SQLException {
        Long userId = rs.getLong(columns.getId());
        if (userId != 0) {
            userIds.add(userId);
        }
    }

    private Genre extractGenreAndSaveToMap(ResultSet rs,
                                           Map<Long, Genre> idToGenre,
                                           GenreTable columns) throws SQLException {
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
        Long mpaId = null;
        if (film.getMpa() != null) {
            throwIfRatingNotFound(film.getMpa());
            mpaId = film.getMpa().getId();
        }
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId());
    }

    private void saveFilmGenresInternal(Film saved) {
        if (filmHasGenres(saved)) {
            List<Genre> genres = saved.getGenres().stream()
                    .distinct()
                    .collect(Collectors.toList());
            genres.forEach(this::throwIfGenreNotFound);
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

    private void throwIfRatingNotFound(Rating r) {
        if (ratingDbStorage.getById(r.getId()).isEmpty()) {
            String msg = String.format("Rating with ID=%d not found!", r.getId());
            throw new ResourceNotFoundException(msg);
        }
    }

    private void throwIfGenreNotFound(Genre g) {
        if (genreDbStorage.getById(g.getId()).isEmpty()) {
            String msg = String.format("Genre with ID=%d not found!", g.getId());
            throw new ResourceNotFoundException(msg);
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
        String sql = sqlProvider.provideInsertFilmSql();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setLong(4, film.getDuration());
            if (film.getMpa() == null) {
                ps.setNull(5, Types.BIGINT);
            } else {
                ps.setLong(5, film.getMpa().getId());
            }
            return ps;
        }, keyHolder);

        Long id = (Long) keyHolder.getKey();
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

                GenreTable genreTable = columnNamesProvider.provideGenreColumns();
                Genre genre = extractGenreAndSaveToMap(rs, idToGenre, genreTable);

                UserTable userTable = columnNamesProvider.provideUserColumns();
                Long userId = extractUserId(rs, userTable);

                FilmTable filmTable = columnNamesProvider.provideFilmColumns();
                Long filmId = rs.getLong(filmTable.getId());
                Film film = idToFilm.get(filmId);
                if (film == null) {
                    film = extractFilm(rs, filmTable, filmId);
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

    private Long extractUserId(ResultSet rs, UserTable userTable) throws SQLException {
        Long userId = rs.getLong(userTable.getId());
        return userId != 0 ? userId : null;
    }
}
