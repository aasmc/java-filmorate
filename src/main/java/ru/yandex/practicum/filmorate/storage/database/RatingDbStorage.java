package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.RatingName;
import ru.yandex.practicum.filmorate.storage.RatingStorage;
import ru.yandex.practicum.filmorate.storage.database.dbutils.SqlProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RatingDbStorage implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SqlProvider sqlProvider;

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
        String sql = sqlProvider.provideInsertRatingSql();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, rating.getName().getRating());
            return ps;
        }, keyHolder);
        Long id = (Long) keyHolder.getKey();
        rating.setId(id);
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
