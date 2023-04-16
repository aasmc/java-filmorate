package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserRepository {

    User save(User user);

    User update(User user);

    List<User> findAll();
}
