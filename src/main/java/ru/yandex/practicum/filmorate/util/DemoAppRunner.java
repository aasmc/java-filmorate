package ru.yandex.practicum.filmorate.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.GenreName;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.RatingName;
import ru.yandex.practicum.filmorate.storage.Constants;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

@Component
@RequiredArgsConstructor
public class DemoAppRunner implements ApplicationRunner {
    @Qualifier(Constants.IN_MEMORY_GENRE_STORAGE)
    private final GenreStorage genreStorage;
    @Qualifier(Constants.IN_MEMORY_RATING_STORAGE)
    private final RatingStorage ratingStorage;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        genreStorage.save(Genre.builder().name(GenreName.COMEDY).build());
        genreStorage.save(Genre.builder().name(GenreName.DRAMA).build());
        genreStorage.save(Genre.builder().name(GenreName.CARTOON).build());
        genreStorage.save(Genre.builder().name(GenreName.THRILLER).build());
        genreStorage.save(Genre.builder().name(GenreName.DOCUMENTARY).build());
        genreStorage.save(Genre.builder().name(GenreName.ACTION).build());

        ratingStorage.save(Rating.builder().name(RatingName.G).build());
        ratingStorage.save(Rating.builder().name(RatingName.PG).build());
        ratingStorage.save(Rating.builder().name(RatingName.PG_13).build());
        ratingStorage.save(Rating.builder().name(RatingName.R).build());
        ratingStorage.save(Rating.builder().name(RatingName.NC_17).build());
    }
}
