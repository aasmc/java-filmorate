package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.util.IdGenerator;

import java.util.*;
import java.util.function.Supplier;

import static ru.yandex.practicum.filmorate.storage.Constants.IN_MEMORY_FILM_STORAGE;

@Component
@Qualifier(IN_MEMORY_FILM_STORAGE)
@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {

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

    @Override
    public void addFriendForUser(Long userId, Long newFriendId) {
        User user = findUserOrThrow(userId);
        User userFriend = findFriendOrThrow(newFriendId);
        user.addFriend(userFriend);
    }


    @Override
    public void removeFriendForUser(Long userId, Long friendId) {
        User user = findUserOrThrow(userId);
        User friend = findFriendOrThrow(friendId);
        user.removeFriend(friend);
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = findUserOrThrow(userId);
        return List.copyOf(user.getFriends());
    }

    @Override
    public List<User> getCommonFriendsForUser(Long userId, Long friendId) {
        User user = findUserOrThrow(userId);
        User friend = findFriendOrThrow(friendId);
        Set<User> commonFriends = new HashSet<>(user.getFriends());
        commonFriends.retainAll(friend.getFriends());
        return List.copyOf(commonFriends);
    }

    @Override
    public User findUserById(Long id) {
        return findUserOrThrow(id);
    }

    private User findUserOrThrow(Long userId) {
        return findUserOrThrow(userId,
                () -> provideNotFoundErrorMessage("User", userId));
    }

    private User findFriendOrThrow(Long friendId) {
        return findUserOrThrow(friendId,
                () -> provideNotFoundErrorMessage("Friend", friendId));
    }

    private String provideNotFoundErrorMessage(String who, Long id) {
        return String.format("%s with ID=%d not found!", who, id);
    }

    private User findUserOrThrow(Long userId, Supplier<String> msgSupplier) {
        return Optional.ofNullable(userMap.get(userId))
                .orElseThrow(() -> new ResourceNotFoundException(msgSupplier.get()));
    }

    private boolean userAlreadyExists(User user) {
        return user.getId() != null && userMap.containsKey(user.getId());
    }

    private boolean userNotFound(User user) {
        return user.getId() == null || !userMap.containsKey(user.getId());
    }
}
