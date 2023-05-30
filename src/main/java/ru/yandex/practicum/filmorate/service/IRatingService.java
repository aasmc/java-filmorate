package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;

public interface IRatingService {

    Rating getRatingById(Long id);

    List<Rating> getAllRatings();

    Rating addRating(Rating rating);
}
