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
import ru.yandex.practicum.filmorate.storage.database.util.FilmColumns;
import ru.yandex.practicum.filmorate.storage.database.util.UserColumns;

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
        String sql = "SELECT f.id film_id, " +
                "f.name film_name, " +
                "f.description film_descr, " +
                "f.release_date film_rel_d, " +
                "f.duration film_dur, " +
                "r.id mpa_id, " +
                "r.name mpa, " +
                "g.id genre_id, " +
                "g.name genre, " +
                "u.id u_id, " +
                "u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd, " +
                "FROM Films f " +
                "LEFT JOIN Ratings r ON f.rating_id = r.id " +
                "LEFT JOIN FilmGenre fg ON f.id = fg.film_id " +
                "LEFT JOIN Genres g ON g.id = fg.genre_id " +
                "LEFT JOIN FilmUserLikes ful ON ful.film_id = f.id " +
                "LEFT JOIN Users u ON u.id = ful.user_id " +
                "LEFT JOIN FriendshipStatus fs ON u.id = fs.user_id " +
                "LEFT JOIN Users uu ON uu.id = fs.friend_id";

        return selectFilmsBySqlInternal(sql);
    }

    private List<Film> selectFilmsBySqlInternal(String sql, Object... args) {
        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Film> idToFilm = new HashMap<>();
            Map<Long, User> idToUser = new HashMap<>();
            Map<Long, Genre> idToGenre = new HashMap<>();
            Rating rating = null;
            while (rs.next()) {

                if (rating == null) {
                    rating = extractRating(rs);
                }

                Long genreId = rs.getLong("genre_id");
                Genre genre = null;
                if (genreId != 0) {
                    genre = idToGenre.get(genreId);
                    if (genre == null) {
                        String genreName = rs.getString("genre");
                        genre = Genre.builder()
                                .id(genreId)
                                .name(GenreName.fromString(genreName))
                                .build();
                        idToGenre.put(genreId, genre);
                    }
                }

                Long uId = rs.getLong("u_id");

                User user = null;
                if (uId != 0L) {
                    user = idToUser.get(uId);
                    if (user == null) { // first time we see the user
                        String uEmail = rs.getString("u_email");
                        String uLogin = rs.getString("u_login");
                        String uName = rs.getString("u_name");
                        LocalDate uBirthday = rs.getDate("u_bd").toLocalDate();
                        user = User.builder()
                                .id(uId)
                                .name(uName)
                                .email(uEmail)
                                .login(uLogin)
                                .birthday(uBirthday)
                                .build();
                        idToUser.put(user.getId(), user);
                    }
                }

                Long friendId = rs.getLong("f_id");
                User friend = null;
                if (friendId != 0L) { // check if we have a friend
                    friend = idToUser.get(friendId);
                    if (friend == null) {
                        String friendEmail = rs.getString("f_email");
                        String friendLogin = rs.getString("f_login");
                        String friendName = rs.getString("f_name");
                        LocalDate friendBd = rs.getDate("f_bd").toLocalDate();
                        friend = User.builder()
                                .id(friendId)
                                .email(friendEmail)
                                .login(friendLogin)
                                .name(friendName)
                                .birthday(friendBd)
                                .build();
                        idToUser.put(friendId, friend);
                    }
                }

                if (friend != null && user != null) {
                    user.addFriend(friend);
                }

                Long filmId = rs.getLong("film_id");
                String filmName = rs.getString("film_name");
                String description = rs.getString("film_descr");
                LocalDate releaseDate = rs.getDate("film_rel_d").toLocalDate();
                long duration = rs.getLong("film_dur");
                Film film = idToFilm.get(filmId);

                if (film == null) {
                    film = Film.builder()
                            .id(filmId)
                            .name(filmName)
                            .description(description)
                            .releaseDate(releaseDate)
                            .duration(duration)
                            .build();
                    // set rating only once since it is one-to-one
                    if (rating != null) {
                        film.setMpa(rating);
                    }
                    idToFilm.put(filmId, film);
                }
                if (genre != null) {
                    film.getGenres().add(genre);
                }
                if (user != null) {
                    film.getUserLikes().add(user);
                }
            }
            return List.copyOf(idToFilm.values());
        }, args);
    }

    @Override
    public void addUserLikeToFilm(Long userId, Long filmId) {
        throwIfFilmNotFound(filmId);
        throwIfUserNotFound(userId);

        String sql = "INSERT INTO FilmUserLikes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeUserLike(Long userId, Long filmId) {
        throwIfUserNotFound(userId);
        throwIfFilmNotFound(filmId);

        String sql = "DELETE FROM FilmUserLikes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(long count) {
        String sql = "WITH top_id_to_count AS ( " +
                "SELECT ff.id t_id, " +
                "COUNT(ful.film_id) cnt " +
                "FROM Films ff " +
                "LEFT JOIN FilmUserLikes ful ON ff.id = ful.film_id " +
                "GROUP BY t_id " +
                "ORDER BY cnt DESC " +
                "LIMIT ?" +
                ") " +
                "SELECT f.id film_id, " +
                "f.name f_name, " +
                "f.description f_descr, " +
                "f.release_date f_rel_d, " +
                "f.duration f_dur, " +
                "r.id mpa_id, " +
                "r.name mpa, " +
                "g.id genre_id, " +
                "g.name genre, " +
                "u.id u_id, " +
                "u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd, " +
                "FROM Films f " +
                "LEFT JOIN Ratings r ON f.rating_id = r.id " +
                "LEFT JOIN FilmGenre fg ON f.id = fg.film_id " +
                "LEFT JOIN Genres g ON g.id = fg.genre_id " +
                "LEFT JOIN FilmUserLikes ful ON ful.film_id = f.id " +
                "LEFT JOIN Users u ON u.id = ful.user_id " +
                "LEFT JOIN FriendshipStatus fs ON u.id = fs.user_id " +
                "LEFT JOIN Users uu ON uu.id = fs.friend_id " +
                "WHERE f.id IN (SELECT t_id FROM top_id_to_count)";
        return selectFilmsBySqlInternal(sql, count);
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        String sql = "SELECT f.id film_id" +
                "f.name film_name, " +
                "f.description film_descr, " +
                "f.release_date film_rel_d, " +
                "f.duration film_dur, " +
                "r.id mpa_id, " +
                "r.name mpa, " +
                "g.id genre_id, " +
                "g.name genre, " +
                "u.id u_id, " +
                "u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd, " +
                "FROM Films f " +
                "LEFT JOIN Ratings r ON f.rating_id = r.id " +
                "LEFT JOIN FilmGenre fg ON f.id = fg.film_id " +
                "LEFT JOIN Genres g ON g.id = fg.genre_id " +
                "LEFT JOIN FilmUserLikes ful ON ful.film_id = f.id " +
                "LEFT JOIN Users u ON u.id = ful.user_id " +
                "LEFT JOIN FriendshipStatus fs ON u.id = fs.user_id " +
                "LEFT JOIN Users uu ON uu.id = fs.friend_id " +
                "WHERE f.id = ?";

        Film film = jdbcTemplate.query(sql, rs -> {
            Film extractedFilm = null;
            Map<Long, User> idToUser = new HashMap<>();
            Map<Long, Genre> idToGenre = new HashMap<>();
            Rating rating = null;
            while (rs.next()) {

                if (rating == null) {
                    rating = extractRating(rs);
                }

                extractGenreAndSaveToMap(rs, idToGenre);
                UserColumns columns = getUserColumns();
                User user = extractUserAndSaveToMap(rs, idToUser, columns);

                UserColumns friendColumns = getFriendColumns();
                User friend = extractUserAndSaveToMap(rs, idToUser, friendColumns);

                if (friend != null && user != null) {
                    user.addFriend(friend);
                }

                if (extractedFilm == null) {
                    extractedFilm = extractFilm(rs, getFilmColumns());
                }
            }

            if (extractedFilm != null) {
                List<Genre> genres = getUniqueGenresSortedById(idToGenre);
                extractedFilm.setGenres(genres);
                Set<User> userLikes = getNonNullUsers(idToUser);
                extractedFilm.setUserLikes(userLikes);
                if (rating != null) {
                    extractedFilm.setMpa(rating);
                }
            }
            return extractedFilm;
        }, filmId);

        return Optional.ofNullable(film);
    }

    private Set<User> getNonNullUsers(Map<Long, User> idToUser) {
        return idToUser.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<Genre> getUniqueGenresSortedById(Map<Long, Genre> idToGenre) {
        return idToGenre.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparingLong(Genre::getId))
                .collect(Collectors.toList());
    }

    private Film extractFilm(ResultSet rs, FilmColumns columns) throws SQLException {
        Long id = rs.getLong(columns.getId());
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


    private FilmColumns getFilmColumns() {
        return FilmColumns.builder()
                .id("film_id")
                .name("film_name")
                .descr("film_descr")
                .releaseDate("film_rel_d")
                .duration("film_dur")
                .build();
    }

    private UserColumns getUserColumns() {
        return UserColumns.builder()
                .id("u_id")
                .email("u_email")
                .login("u_login")
                .name("u_name")
                .birthday("u_bd")
                .build();
    }

    private UserColumns getFriendColumns() {
        return UserColumns.builder()
                .id("f_id")
                .email("f_email")
                .login("f_login")
                .name("f_name")
                .birthday("f_bd")
                .build();
    }

    private Rating extractRating(ResultSet rs) throws SQLException {
        Rating rating = null;
        Long mpaId = rs.getLong("mpa_id");
        if (mpaId != 0) {
            String mpa = rs.getString("mpa");
            rating = Rating.builder()
                    .id(mpaId)
                    .name(RatingName.fromString(mpa))
                    .build();
        }
        return rating;
    }

    private User extractUserAndSaveToMap(ResultSet rs,
                                         Map<Long, User> idToUser,
                                         UserColumns columns) throws SQLException {
        User user = null;
        Long uId = rs.getLong(columns.getId());
        if (uId != 0L) {
            user = idToUser.get(uId);
            if (user == null) { // first time we see the user
                String uEmail = rs.getString(columns.getEmail());
                String uLogin = rs.getString(columns.getLogin());
                String uName = rs.getString(columns.getName());
                LocalDate uBirthday = rs.getDate(columns.getBirthday()).toLocalDate();
                user = User.builder()
                        .id(uId)
                        .name(uName)
                        .email(uEmail)
                        .login(uLogin)
                        .birthday(uBirthday)
                        .build();
                idToUser.put(user.getId(), user);
            }
        }
        return user;
    }


    private void extractGenreAndSaveToMap(ResultSet rs, Map<Long, Genre> idToGenre) throws SQLException {
        Long genreId = rs.getLong("genre_id");
        if (genreId != 0) {
            Genre genre = idToGenre.get(genreId);
            if (genre == null) {
                String genreName = rs.getString("genre");
                genre = Genre.builder()
                        .id(genreId)
                        .name(GenreName.fromString(genreName))
                        .build();
                idToGenre.put(genreId, genre);
            }
        }
    }

    @Override
    public Optional<Long> checkFilmId(Long id) {
        String sql = "SELECT id FROM Films WHERE id = ?";
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

    private void updateFilmInternal(Film film) {
        String sql = "UPDATE Films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, rating_id = ? WHERE id = ?";
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
            String sql = "INSERT INTO FilmGenre (film_id, genre_id) VALUES (?, ?)";
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
            String sql = "INSERT INTO FilmUserLikes (film_id, user_id) VALUES (?, ?)";
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
            parameters.put("rating_id", film.getMpa().getId());
        }
        Long id = (Long) filmsJdbcInsert.executeAndReturnKey(parameters);
        film.setId(id);
        return film;
    }

    private void deleteFilmUserLikesInternal(Film film) {
        String sql = "DELETE FROM FilmUserLikes WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getId());
    }

    private void deleteFilmGenresInternal(Film film) {
        String sql = "DELETE FROM FilmGenre WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getId());
    }
}
