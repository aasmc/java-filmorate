package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User save(User user);

    User update(User user);

    List<User> findAll();

    void addFriendForUser(Long userId, Long newFriendId);

    void removeFriendForUser(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriendsForUser(Long userId, Long friendId);

    User findUserById(Long id);
}
