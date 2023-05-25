package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;
import java.util.Optional;

public interface RatingStorage {

    List<Rating> findAll();

    Optional<Rating> getById(Long id);

    Rating save(Rating rating);

}
