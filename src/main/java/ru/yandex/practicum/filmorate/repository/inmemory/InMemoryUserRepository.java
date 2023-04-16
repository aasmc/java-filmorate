package ru.yandex.practicum.filmorate.repository.inmemory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.util.IdGenerator;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> userMap;
    private final IdGenerator idGenerator;

    @Override
    public User save(User user) {
        if (userAlreadyExists(user)) {
            String msg = String.format("User with ID: %d already exists", user.getId());
            throw new ResourceAlreadyExistsException(msg);
        }
        user.setId(idGenerator.nextId());
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (userNotFound(user)) {
            String msg = String.format("User with ID: %d is not found.", user.getId());
            throw new ResourceNotFoundException(msg);
        }
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(userMap.values());
    }

    private boolean userAlreadyExists(User user) {
        return user.getId() != null && userMap.containsKey(user.getId());
    }

    private boolean userNotFound(User user) {
        return user.getId() == null || !userMap.containsKey(user.getId());
    }
}
