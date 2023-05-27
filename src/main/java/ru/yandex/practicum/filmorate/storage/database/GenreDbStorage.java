package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.GenreName;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.database.dbutils.SqlProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.storage.Constants.DB_GENRE_STORAGE;

@Component
@Qualifier(DB_GENRE_STORAGE)
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SqlProvider sqlProvider;
    private SimpleJdbcInsert simpleJdbcInsert;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, SqlProvider sqlProvider) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("Genres")
                .usingGeneratedKeyColumns("id");
        this.sqlProvider = sqlProvider;
    }

    @Override
    public List<Genre> findAll() {
        String sql = sqlProvider.provideFindAllGenresSql();
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs));
    }

    @Override
    public Optional<Genre> getById(Long id) {
        String sql = sqlProvider.provideFindGenreByIdSql();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (rowSet.next()) {
             return Optional.of(Genre.builder()
                    .id(rowSet.getLong("id"))
                    .name(GenreName.fromString(rowSet.getString("name")))
                    .build());
        }
        return Optional.empty();
    }

    @Override
    public Genre save(Genre genre) {
        if (genreAlreadyExists(genre)) {
            String msg = String.format("Genre already exists: %s", genre.getName().getGenre());
            throw new ResourceAlreadyExistsException(msg);
        }
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", genre.getName().getGenre());
        Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        genre.setId((Long) id);
        return genre;
    }

    private boolean genreAlreadyExists(Genre genre) {
        return genre.getId() != null && getById(genre.getId()).isPresent();
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getLong("id"))
                .name(GenreName.fromString(rs.getString("name")))
                .build();
    }
}
