package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.RatingName;
import ru.yandex.practicum.filmorate.storage.RatingStorage;
import ru.yandex.practicum.filmorate.storage.database.dbutils.SqlProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.storage.Constants.DB_RATING_STORAGE;

@Component
@Qualifier(DB_RATING_STORAGE)
public class RatingDbStorage implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SqlProvider sqlProvider;
    private SimpleJdbcInsert simpleJdbcInsert;

    public RatingDbStorage(JdbcTemplate jdbcTemplate, SqlProvider sqlProvider) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("Ratings")
                .usingGeneratedKeyColumns("id");
        this.sqlProvider = sqlProvider;
    }

    @Override
    public List<Rating> findAll() {
        String sql = sqlProvider.provideFindAllRatingsSql();
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeRating(rs));
    }

    @Override
    public Optional<Rating> getById(Long id) {
        String sql = sqlProvider.provideFindRatingByIdSql();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (rowSet.next()) {
            return Optional.of(Rating.builder()
                    .id(rowSet.getLong("id"))
                    .name(RatingName.fromString(rowSet.getString("name")))
                    .build());
        }
        return Optional.empty();
    }

    @Override
    public Rating save(Rating rating) {
        if (ratingAlreadyExists(rating)) {
            String msg = String.format("Rating already exists: '%s'.", rating.getName().getRating());
            throw new ResourceAlreadyExistsException(msg);
        }
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", rating.getName().getRating());
        Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        rating.setId((Long) id);
        return rating;
    }

    private boolean ratingAlreadyExists(Rating rating) {
        return rating.getId() != null && getById(rating.getId()).isPresent();
    }

    private Rating makeRating(ResultSet rs) throws SQLException {
        return Rating.builder()
                .id(rs.getLong("id"))
                .name(RatingName.fromString(rs.getString("name")))
                .build();
    }
}
