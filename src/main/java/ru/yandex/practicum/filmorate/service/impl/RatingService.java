package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.IRatingService;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.storage.Constants.IN_MEMORY_RATING_STORAGE;

@Service
@RequiredArgsConstructor
public class RatingService implements IRatingService {

    @Qualifier(IN_MEMORY_RATING_STORAGE)
    private final RatingStorage ratingStorage;

    @Override
    public Rating getRatingById(Long id) {
        return ratingStorage.getById(id);
    }

    @Override
    public List<Rating> getAllRatings() {
        return ratingStorage.findAll();
    }

    @Override
    public Rating addRating(Rating rating) {
        return ratingStorage.save(rating);
    }
}
