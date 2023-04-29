package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface IUserService {

    List<User> findAllUsers();

    User createUser(User user);

    User updateUser(User user);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> findCommonFriendsWith(Long userId, Long friendId);

    List<User> findFriendsOfUser(Long userId);

    User getUserById(Long userId);

}
