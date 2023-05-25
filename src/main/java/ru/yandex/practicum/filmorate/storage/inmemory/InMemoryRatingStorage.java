package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;
import ru.yandex.practicum.filmorate.util.IdGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.storage.Constants.IN_MEMORY_RATING_STORAGE;

@Component
@RequiredArgsConstructor
@Qualifier(IN_MEMORY_RATING_STORAGE)
public class InMemoryRatingStorage implements RatingStorage {

    private final IdGenerator idGenerator;
    private final Map<Long, Rating> ratingMap;


    @Override
    public List<Rating> findAll() {
        return List.copyOf(ratingMap.values());
    }

    @Override
    public Optional<Rating> getById(Long id) {
        return Optional.ofNullable(ratingMap.get(id));
    }

    @Override
    public Rating save(Rating rating) {
        if (ratingAlreadyExists(rating)) {
            String msg = String.format("Rating already exists: '%s'.", rating.getName().getRating());
            throw new ResourceAlreadyExistsException(msg);
        }
        rating.setId(idGenerator.nextId());
        ratingMap.put(rating.getId(), rating);
        return rating;
    }

    private boolean ratingAlreadyExists(Rating rating) {
        return rating.getId() != null && ratingMap.containsKey(rating.getId());
    }
}
