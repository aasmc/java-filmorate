package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.IUserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        log.debug("Creating user {}", user);
        checkAndSetUserNameIfNeeded(user);
        User created = userService.createUser(user);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<User> updateUser(@RequestBody @Valid User user) {
        log.debug("Updating user {}", user);
        checkAndSetUserNameIfNeeded(user);
        User updated = userService.updateUser(user);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void addFriend(@PathVariable("id") Long userId,
                          @PathVariable("friendId") Long friendId) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeFriend(@PathVariable("id") Long userId,
                             @PathVariable("friendId") Long friendId) {
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<User>> getFriendsOfUser(@PathVariable("id") Long id) {
        List<User> users = userService.findFriendsOfUser(id);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<User>> getCommonFriendsOfUsers(@PathVariable("id") Long userId,
                                              @PathVariable("otherId") Long friendId)  {
        List<User> users = userService.findCommonFriendsWith(userId, friendId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    private void checkAndSetUserNameIfNeeded(User user) {
        if (ObjectUtils.isEmpty(user.getName())) {
            user.setName(user.getLogin());
        }
    }

}
