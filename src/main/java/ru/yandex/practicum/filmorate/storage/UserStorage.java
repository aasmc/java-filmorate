package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User save(User user);

    User update(User user);

    List<User> findAll();

    void addFriendForUser(Long userId, Long newFriendId);

    void removeFriendForUser(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriendsForUser(Long userId, Long friendId);

    Optional<User> findUserById(Long id);

    Optional<Long> checkUserId(Long id);

    default boolean userAlreadyExists(Long userId) {
        return userId != null && checkUserId(userId).isPresent();
    }

    default boolean userNotFound(Long userId) {
        return userId == null || checkUserId(userId).isEmpty();
    }
}
