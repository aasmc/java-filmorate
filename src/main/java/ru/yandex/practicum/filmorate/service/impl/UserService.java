package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.IUserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    @Override
    public List<User> findAllUsers() {
        return userStorage.findAll();
    }

    @Override
    public User createUser(User user) {
        return userStorage.save(user);
    }

    @Override
    public User updateUser(User user) {
        return userStorage.update(user);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        userStorage.addFriendForUser(userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        userStorage.removeFriendForUser(userId, friendId);
    }

    @Override
    public List<User> findCommonFriendsWith(Long userId, Long friendId) {
        return userStorage.getCommonFriendsForUser(userId, friendId);
    }

    @Override
    public List<User> findFriendsOfUser(Long userId) {
        return userStorage.getFriends(userId);
    }

    @Override
    public User getUserById(Long userId) {
        return userStorage.findUserById(userId)
                .orElseThrow(() -> {
                    String msg = String.format("User with ID = %d not found.", userId);
                    return new ResourceNotFoundException(msg);
                });
    }

}
