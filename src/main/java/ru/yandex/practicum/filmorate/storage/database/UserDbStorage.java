package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.storage.Constants.DB_USER_STORAGE;

@Component
@Qualifier(DB_USER_STORAGE)
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert usersJdbcInsert;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        usersJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("Users")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public User save(User user) {
        throwIfUserExists(user.getId());

        User saved = saveUserInternal(user);
        saveUserFriendsInternal(saved);
        // return user with all info about him/her
        return findUserById(saved.getId()).get();
    }

    private void saveUserFriendsInternal(User saved) {
        String sql = "insert into FriendshipStatus (user_id, friend_id) " +
                "values (?, ?)";
        jdbcTemplate.batchUpdate(
                sql,
                saved.getFriends(),
                saved.getFriends().size(),
                (PreparedStatement ps, User u) -> {
                    ps.setLong(1, saved.getId());
                    ps.setLong(2, u.getId());
                }
        );
    }

    private User saveUserInternal(User user) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName());
        parameters.put("birthday", user.getBirthday());
        Long id = (Long) usersJdbcInsert.executeAndReturnKey(parameters);
        user.setId(id);
        return user;
    }


    @Override
    public User update(User user) {
        throwIfUserNotFound(user.getId());

        updateUserInternal(user);
        deleteUserFriendsInternal(user);
        saveUserFriendsInternal(user);
        return findUserById(user.getId()).get();
    }

    private void updateUserInternal(User user) {
        String sql = "update Users set email = ?, login = ?, name = ?, birthday = ? " +
                "where id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
    }

    private void deleteUserFriendsInternal(User user) {
        String sql = "delete from FriendshipStatus where user_id = ?";
        jdbcTemplate.update(sql, user.getId());
    }

    @Override
    public List<User> findAll() {
        String sql = "select u.id u_id, u.email u_email, u.login u_login, u.name u_name, u.birthday u_bd, " +
                "uu.id f_id, uu.email f_email, uu.login f_login, uu.name f_name, uu.birthday f_bd from Users u " +
                "left join FriendshipStatus fs on u.id = fs.user_id " +
                "left join Users uu on uu.id = fs.friend_id";

        return selectUsersBySqlInternal(sql);
    }

    @Override
    public void addFriendForUser(Long userId, Long newFriendId) {
        throwIfUserNotFound(userId);
        throwIfUserNotFound(newFriendId);

        String sql = "insert into FriendshipStatus (user_id, friend_id) " +
                "values (?, ?)";

        jdbcTemplate.update(sql, userId, newFriendId);
    }

    @Override
    public void removeFriendForUser(Long userId, Long friendId) {
        throwIfUserNotFound(userId);
        throwIfUserNotFound(friendId);

        String sql = "delete from FriendshipStatus where user_id = ? and friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        throwIfUserNotFound(userId);

        String sql = "select u.id u_id, u.email u_email, u.login u_login, u.name u_name, u.birthday u_bd, " +
                "uu.id f_id, uu.email f_email, uu.login f_login, uu.name f_name, uu.birthday f_bd from Users u " +
                "left join FriendshipStatus fs on u.id = fs.user_id " +
                "left join Users uu on uu.id = fs.friend_id " +
                "where u.id in (select ffs.friend_id from Users uuu " +
                "inner join FriendshipStatus ffs on uuu.id = ffs.user_id " +
                "where uuu.id = ?)";

        return selectUsersBySqlInternal(sql, userId);
    }

    @Override
    public List<User> getCommonFriendsForUser(Long userId, Long friendId) {
        throwIfUserNotFound(userId);
        throwIfUserNotFound(friendId);

        String sql = "with common_friend_ids as ( " +
                "select f.friend_id f_id from Users u " +
                "inner join FriendshipStatus f on u.id = f.user_id " +
                "where u.id = ? " +
                "intersect " +
                "select ff.friend_id f_id from Users uu " +
                "inner join FriendshipStatus ff on uu.id = ff.user_id " +
                "where uu.id = ?) " +
                "select u.id u_id, u.email u_email, u.login u_login, u.name u_name, u.birthday u_bd, " +
                "uu.id f_id, uu.email f_email, uu.login f_login, uu.name f_name, uu.birthday f_bd from Users u " +
                "left join FriendshipStatus fs on u.id = fs.user_id " +
                "left join Users uu on uu.id = fs.friend_id " +
                "where u.id in (select f_id from common_friend_ids)";

        return selectUsersBySqlInternal(sql, userId, friendId);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        String sql = "select u.email u_email, u.login u_login, u.name u_name, u.birthday u_bd, " +
                "uu.id f_id, uu.email f_email, uu.login f_login, uu.name f_name, uu.birthday f_bd from Users u " +
                "left join FriendshipStatus fs on u.id = fs.user_id " +
                "left join Users uu on uu.id = fs.friend_id " +
                "where u.id = ?";
        User user = jdbcTemplate.query(sql, rs -> {
            User u = null;
            while (rs.next()) {
                String email = rs.getString("u_email");
                String login = rs.getString("u_login");
                String name = rs.getString("u_name");
                LocalDate birthday = rs.getDate("u_bd").toLocalDate();

                Long friendId = rs.getLong("f_id");
                User friend = null;
                if (friendId != 0L) {
                    String friendEmail = rs.getString("f_email");
                    String friendLogin = rs.getString("f_login");
                    String friendName = rs.getString("f_name");
                    LocalDate friendBd = rs.getDate("f_bd").toLocalDate();
                    friend = User.builder()
                            .id(friendId)
                            .email(friendEmail)
                            .login(friendLogin)
                            .name(friendName)
                            .birthday(friendBd)
                            .build();
                }
                if (u == null) {
                    u = User.builder()
                            .id(id)
                            .name(name)
                            .email(email)
                            .login(login)
                            .birthday(birthday)
                            .build();
                }
                if (friend != null) {
                    u.addFriend(friend);
                }
            }
            return u;
        }, id);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<Long> checkUserId(Long id) {
        String sql = "select id from Users where id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (rowSet.next()) {
            return Optional.of(rowSet.getLong("id"));
        }
        return Optional.empty();
    }

    private void throwIfUserNotFound(Long userId) {
        if (userNotFound(userId)) {
            String msg = String.format("User with ID: %d is not found.", userId);
            throw new ResourceNotFoundException(msg);
        }
    }

    private void throwIfUserExists(Long userId) {
        if (userAlreadyExists(userId)) {
            String msg = String.format("User with ID: %d already exists", userId);
            throw new ResourceAlreadyExistsException(msg);
        }
    }

    private List<User> selectUsersBySqlInternal(String sql, Object... args) {
        return jdbcTemplate.query(sql, rs -> {
            Map<Long, User> idToUser = new HashMap<>();
            while (rs.next()) {
                Long id = rs.getLong("u_id");
                String email = rs.getString("u_email");
                String login = rs.getString("u_login");
                String name = rs.getString("u_name");
                LocalDate birthday = rs.getDate("u_bd").toLocalDate();

                Long friendId = rs.getLong("f_id");
                User friend = null;
                if (friendId != 0L) { // check if we have a friend
                    String friendEmail = rs.getString("f_email");
                    String friendLogin = rs.getString("f_login");
                    String friendName = rs.getString("f_name");
                    LocalDate friendBd = rs.getDate("f_bd").toLocalDate();
                    friend = User.builder()
                            .id(friendId)
                            .email(friendEmail)
                            .login(friendLogin)
                            .name(friendName)
                            .birthday(friendBd)
                            .build();
                    idToUser.put(friend.getId(), friend);
                }

                User user = idToUser.get(id);
                if (user == null) { // first time we see the user
                    user = User.builder()
                            .id(id)
                            .name(name)
                            .email(email)
                            .login(login)
                            .birthday(birthday)
                            .build();
                    idToUser.put(user.getId(), user);
                }
                if (friend != null) {
                    user.addFriend(friend);
                }
            }
            return List.copyOf(idToUser.values());
        }, args);
    }
}
