package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.filmorate.testutil.TestDataProvider.*;
import static ru.yandex.practicum.filmorate.testutil.TestDataProvider.newUser;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(
        scripts = "classpath:db/clean-users.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class UserDbStorageTest {

    private final UserDbStorage userDbStorage;

    @Test
    void whenFindExistingUserById_returnsNonEmptyOptional() {
        User user = newUser();
        user = userDbStorage.save(user);

        Optional<User> userById = userDbStorage.findUserById(user.getId());
        assertThat(userById).isNotEmpty();
        User retrieved = userById.get();
        assertThat(retrieved).isEqualTo(user);
    }

    @Test
    void whenFindNonExistingUserById_returnsEmptyOptional() {
        Optional<User> userById = userDbStorage.findUserById(100L);
        assertThat(userById).isEmpty();
    }

    @Test
    void whenGetCommonFriendsOfUsersWithCommonFriends_returnsListOfCommonFriends() {
        User user = newUser();
        user = userDbStorage.save(user);
        User friend1 = newUser();
        friend1 = userDbStorage.save(friend1);
        userDbStorage.addFriendForUser(user.getId(), friend1.getId());

        User user2 = newUser();
        user2 = userDbStorage.save(user2);
        User friend2 = newUser();
        friend2 = userDbStorage.save(friend2);
        userDbStorage.addFriendForUser(user2.getId(), friend2.getId());

        User common = newUser();
        common = userDbStorage.save(common);
        userDbStorage.addFriendForUser(user.getId(), common.getId());
        userDbStorage.addFriendForUser(user2.getId(), common.getId());

        List<User> friends = userDbStorage.getCommonFriendsForUser(user.getId(), user2.getId());
        assertThat(friends).containsExactly(common);
    }

    @Test
    void whenGetCommonFriendsOfUsersThatHaveNoCommonFriends_returnsEmptyList() {
        User user = newUser();
        user = userDbStorage.save(user);
        User friend1 = newUser();
        friend1 = userDbStorage.save(friend1);
        userDbStorage.addFriendForUser(user.getId(), friend1.getId());

        User user2 = newUser();
        user2 = userDbStorage.save(user2);
        User friend2 = newUser();
        friend2 = userDbStorage.save(friend2);
        userDbStorage.addFriendForUser(user2.getId(), friend2.getId());

        List<User> friends = userDbStorage.getCommonFriendsForUser(user.getId(), user2.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void whenGetCommonFriendsOfUserAndNonExistingFriend_throws() {
        User user = newUser();
        User saved = userDbStorage.save(user);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userDbStorage.getCommonFriendsForUser(saved.getId(), 101L)
        );
    }

    @Test
    void whenGetCommonFriendsOfNonExistingUser_throws() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userDbStorage.getCommonFriendsForUser(100L, 101L)
        );
    }

    @Test
    void whenGetFriendsOfUserWithFriends_returnsListOfFriends() {
        User user = newUser();
        user = userDbStorage.save(user);

        User friend1 = newUser();
        friend1 = userDbStorage.save(friend1);

        User friend2 = newUser();
        friend2 = userDbStorage.save(friend2);

        userDbStorage.addFriendForUser(user.getId(), friend1.getId());
        userDbStorage.addFriendForUser(user.getId(), friend2.getId());

        List<User> friends = userDbStorage.getFriends(user.getId());
        assertThat(friends).containsAll(List.of(friend1, friend2));
    }

    @Test
    void whenGetFriendsOfUserWithNoFriends_returnsEmptyList() {
        User user = newUser();
        user = userDbStorage.save(user);

        List<User> friends = userDbStorage.getFriends(user.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void whenGetFriendsOfNonExistingUser_throws() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userDbStorage.getFriends(100L)
        );
    }

    @Test
    void whenRemoveFriendForUser_userNotContainsThisFriendAmongFriends() {
        User user = newUser();
        user = userDbStorage.save(user);

        User friend = newUser();
        friend = userDbStorage.save(friend);

        userDbStorage.addFriendForUser(user.getId(), friend.getId());

        userDbStorage.removeFriendForUser(user.getId(), friend.getId());

        User retrieved = userDbStorage.findUserById(user.getId()).get();
        assertThat(retrieved.getFriends()).isEmpty();
    }

    @Test
    void whenRemoveNonExistingFriendForUser_throws() {
        User user = newUser();
        User saved = userDbStorage.save(user);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userDbStorage.removeFriendForUser(saved.getId(), 101L)
        );
    }

    @Test
    void whenRemoveFriendForNonExistingUser_throws() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userDbStorage.removeFriendForUser(100L, 101L)
        );
    }

    @Test
    void whenAddFriendForUser_friendshipInfoReturnedWhenQueryForUser_friendNotContainsUserAsFriend() {
        User user = newUser();
        user = userDbStorage.save(user);

        User friend = newUser();
        friend = userDbStorage.save(friend);

        userDbStorage.addFriendForUser(user.getId(), friend.getId());

        Optional<User> userById = userDbStorage.findUserById(user.getId());
        assertThat(userById).isPresent();

        User retrieved = userById.get();
        assertThat(retrieved.getFriends()).contains(friend);

        User retrievedFriend = userDbStorage.findUserById(friend.getId()).get();
        assertThat(retrievedFriend.getFriends()).isEmpty();
    }

    @Test
    void whenAddNonExistingFriendForExistingUser_throws() {
        User user = newUser();
        User saved = userDbStorage.save(user);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userDbStorage.addFriendForUser(saved.getId(), 101L)
        );
    }

    @Test
    void whenAddFriendForNonExistingUser_throws() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userDbStorage.addFriendForUser(100L, 101L)
        );
    }

    @Sql(
            scripts = "classpath:db/users-data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    void whenFindAllOnNonEmptyDb_returnsListOfUsers() {
        List<User> all = userDbStorage.findAll();
        List<User> expected = List.of(
                userOne(),
                userTwo(),
                userThree()
        );
        assertThat(all).hasSize(3);
        assertThat(all).containsAll(expected);
    }

    @Test
    void whenFindAllOnEmptyDb_returnsEmptyList() {
        List<User> all = userDbStorage.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void whenUpdateExistingUserWithExistingFriend_returnsUpdatedUser() {
        User user = newUser();
        user = userDbStorage.save(user);

        User friend = newUser();
        friend = userDbStorage.save(friend);

        User toUpdate = user.setName("Updated Name")
                .setLogin("Updated Login")
                .setEmail("updated@email.com")
                .setFriends(Set.of(friend));

        User updated = userDbStorage.update(toUpdate);
        assertThat(updated.getId()).isEqualTo(user.getId());
        assertThat(updated.getName()).isEqualTo(toUpdate.getName());
        assertThat(updated.getLogin()).isEqualTo(toUpdate.getLogin());
        assertThat(updated.getEmail()).isEqualTo(toUpdate.getEmail());
        assertThat(updated.getFriends()).contains(friend);
    }

    @Test
    void whenUpdateExistingUserWithNonExistingFriends_throws() {
        User user = newUser();
        user = userDbStorage.save(user);

        User toUpdate = user.setName("Updated Name")
                .setLogin("Updated Login")
                .setEmail("updated@email.com")
                .setFriends(Set.of(newUser().setId(999L)));

        assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    userDbStorage.update(toUpdate);
                }
        );
    }

    @Test
    void whenUpdateExistingUser_returnsUpdatedUser() {
        User user = newUser();
        user = userDbStorage.save(user);

        User toUpdate = user.setName("Updated Name")
                .setLogin("Updated Login")
                .setEmail("updated@email.com");

        User updated = userDbStorage.update(toUpdate);
        assertThat(updated.getId()).isEqualTo(user.getId());
        assertThat(updated.getName()).isEqualTo(toUpdate.getName());
        assertThat(updated.getLogin()).isEqualTo(toUpdate.getLogin());
        assertThat(updated.getEmail()).isEqualTo(toUpdate.getEmail());
    }

    @Test
    void whenUpdateNonExistingUser_throws() {
        User newUser = newUser();
        assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    userDbStorage.update(newUser);
                }
        );
    }

    @Test
    void whenSaveNewUserWithFriendNotInDb_throws() {
        User newUser = newUser();
        newUser.addFriend(newUser().setId(999L));
        assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    userDbStorage.save(newUser);
                }
        );
    }

    @Test
    void whenSaveUserThatExistsInDb_throws() {
        User toBeSaved = newUser();
        User saved = userDbStorage.save(toBeSaved);

        User newUser = newUser();
        newUser.setId(saved.getId());
        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> {
                    userDbStorage.save(newUser);
                }
        );
    }

    @Test
    void whenSaveNonExistentUser_createsUser_returnsUserWithId() {
        User newUser = newUser();
        User saved = userDbStorage.save(newUser);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(saved.getName()).isEqualTo(newUser.getName());
        assertThat(saved.getLogin()).isEqualTo(newUser.getLogin());
        assertThat(saved.getBirthday()).isEqualTo(newUser.getBirthday());
        assertThat(saved.getFriends()).isEmpty();
    }

}