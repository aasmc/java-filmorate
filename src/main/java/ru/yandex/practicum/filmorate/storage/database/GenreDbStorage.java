package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.GenreName;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.database.dbutils.SqlProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SqlProvider sqlProvider;

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
        String sql = sqlProvider.provideInsertGenreSql();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, genre.getName().getGenre());
            return ps;
        }, keyHolder);
        Long id = (Long) keyHolder.getKey();
        genre.setId(id);
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
