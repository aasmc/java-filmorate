package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.RatingName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RatingDbStorageTest {

    private final RatingDbStorage ratingDbStorage;

    @Test
    void findAll_returnsAllRatings() {
        List<Rating> expected = List.of(
                Rating.builder().id(1L).name(RatingName.G).build(),
                Rating.builder().id(2L).name(RatingName.PG).build(),
                Rating.builder().id(3L).name(RatingName.PG_13).build(),
                Rating.builder().id(4L).name(RatingName.R).build(),
                Rating.builder().id(5L).name(RatingName.NC_17).build()
        );

        List<Rating> all = ratingDbStorage.findAll();
        assertThat(all).isEqualTo(expected);
    }

    @Test
    void getById_returnsRating() {
        Rating expected = Rating.builder().id(1L).name(RatingName.G).build();
        Optional<Rating> byId = ratingDbStorage.getById(1L);
        assertThat(byId).isPresent();
        Rating rating = byId.get();
        assertThat(rating).isEqualTo(expected);
    }

    @Test
    void getById_returnsEmptyOptional_givenInvalidId() {
        Optional<Rating> byId = ratingDbStorage.getById(10L);
        assertThat(byId).isEmpty();
    }

}