package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ResourceAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.dbutils.ColumnNamesProvider;
import ru.yandex.practicum.filmorate.storage.database.dbutils.SqlProvider;
import ru.yandex.practicum.filmorate.storage.database.dbutils.UserColumns;

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
    private final SqlProvider sqlProvider;
    private final ColumnNamesProvider columnNamesProvider;
    private SimpleJdbcInsert usersJdbcInsert;

    public UserDbStorage(JdbcTemplate jdbcTemplate,
                         SqlProvider sqlProvider,
                         ColumnNamesProvider columnNamesProvider) {
        this.jdbcTemplate = jdbcTemplate;
        usersJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("Users")
                .usingGeneratedKeyColumns("id");
        this.sqlProvider = sqlProvider;
        this.columnNamesProvider = columnNamesProvider;
    }

    @Transactional
    @Override
    public User save(User user) {
        throwIfUserExists(user.getId());

        User saved = saveUserInternal(user);
        saveUserFriendsInternal(saved);
        // return user with all info about him/her
        return findUserById(saved.getId()).get();
    }

    private void saveUserFriendsInternal(User saved) {
        saved.getFriends().forEach(f -> {
            throwIfUserNotFound(f.getId());
        });
        String sql = sqlProvider.provideSaveUserFriendsSql();
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


    @Transactional
    @Override
    public User update(User user) {
        throwIfUserNotFound(user.getId());

        updateUserInternal(user);
        deleteUserFriendsInternal(user);
        saveUserFriendsInternal(user);
        return findUserById(user.getId()).get();
    }

    private void updateUserInternal(User user) {
        String sql = sqlProvider.provideUpdateUserSql();
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
    }

    private void deleteUserFriendsInternal(User user) {
        String sql = sqlProvider.provideDeleteUserFriendsSql();
        jdbcTemplate.update(sql, user.getId());
    }

    @Override
    public List<User> findAll() {
        String sql = sqlProvider.provideFindAllUsersSql();
        return selectUsersBySqlInternal(sql);
    }

    @Override
    public void addFriendForUser(Long userId, Long newFriendId) {
        throwIfUserNotFound(userId);
        throwIfUserNotFound(newFriendId);

        String sql = sqlProvider.provideSaveUserFriendsSql();
        jdbcTemplate.update(sql, userId, newFriendId);
    }

    @Override
    public void removeFriendForUser(Long userId, Long friendId) {
        throwIfUserNotFound(userId);
        throwIfUserNotFound(friendId);

        String sql = sqlProvider.provideDeleteSingleUserFriendSql();
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        throwIfUserNotFound(userId);

        String sql = sqlProvider.provideGetFriendsOfUserSql();
        return selectUsersBySqlInternal(sql, userId);
    }

    @Override
    public List<User> getCommonFriendsForUser(Long userId, Long friendId) {
        throwIfUserNotFound(userId);
        throwIfUserNotFound(friendId);

        String sql = sqlProvider.provideGetCommonFriendsOfUsersSql();
        return selectUsersBySqlInternal(sql, userId, friendId);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        String sql = sqlProvider.provideFindUserByIdSql();
        User user = jdbcTemplate.query(sql, rs -> {
            User u = null;
            while (rs.next()) {
                User friend = extractFriend(rs, columnNamesProvider.provideFriendColumns());
                if (u == null) {
                    u = extractUser(rs, columnNamesProvider.provideUserColumns(), id);
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
        String sql = sqlProvider.provideCheckUserIdSql();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (rowSet.next()) {
            return Optional.of(rowSet.getLong("id"));
        }
        return Optional.empty();
    }

    private User extractUser(ResultSet rs, UserColumns columns, Long id) throws SQLException {
        String email = rs.getString(columns.getEmail());
        String login = rs.getString(columns.getLogin());
        String name = rs.getString(columns.getName());
        LocalDate birthday = rs.getDate(columns.getBirthday()).toLocalDate();
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .login(login)
                .birthday(birthday)
                .build();
    }

    private User extractFriend(ResultSet rs, UserColumns columns) throws SQLException {
        User friend = null;
        Long friendId = rs.getLong(columns.getId());
        if (friendId != 0L) {
            String friendEmail = rs.getString(columns.getEmail());
            String friendLogin = rs.getString(columns.getLogin());
            String friendName = rs.getString(columns.getName());
            LocalDate friendBd = rs.getDate(columns.getBirthday()).toLocalDate();
            friend = User.builder()
                    .id(friendId)
                    .email(friendEmail)
                    .login(friendLogin)
                    .name(friendName)
                    .birthday(friendBd)
                    .build();
        }
        return friend;
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
        UserColumns userColumns = columnNamesProvider.provideUserColumns();
        return jdbcTemplate.query(sql, rs -> {
            Map<Long, User> idToUser = new HashMap<>();
            while (rs.next()) {
                User friend = extractFriend(rs, columnNamesProvider.provideFriendColumns());

                if (friend != null) {
                    idToUser.put(friend.getId(), friend);
                }

                Long id = rs.getLong(userColumns.getId());
                User user = idToUser.get(id);
                if (user == null) { // first time we see the user
                    user = extractUser(rs, userColumns, id);
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
