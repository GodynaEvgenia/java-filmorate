package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@AllArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String INSERT_QUERY = "INSERT INTO users(name, login, email, birthday) VALUES (?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String UPDATE_QUERY = "update users set " +
            "name = ?, login = ?, email = ?, birthday = ? " +
            "where id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
    private static final String GET_FRIENDS_QUERY =
            "SELECT * FROM users u, friendship f WHERE f.user_id = ? AND u.id = f.friend_id";
    private static final String DELETE_FRIEND_QUERY =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_FRIEND_BY_USER_QUERY =
            "DELETE FROM friendship WHERE user_id = ? OR friend_id = ?";
    private static final String GET_COMMON_FRIENDS_QUERY =
            "SELECT * from users u where u.id in (" +
                    "SELECT f1.friend_id " +
                    "FROM friendship f1 " +
                    "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
                    "WHERE f1.user_id = ?" +
                    "  AND f2.user_id = ?)";
    private static final String DELETE_USER_BY_ID = "DELETE FROM users WHERE id = ?";

    @Override
    public List<User> getAll() {
        return jdbc.query(FIND_ALL_QUERY, this::mapRowToUser);
    }

    @Override
    public User get(long id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, userRowMapper, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Пользователь не найден с ID: " + id);
        }
    }

    @Override
    public User create(User user) {
        user.validate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY, new String[]{"id"});
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, String.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        get(user.getId());
        jdbc.update(UPDATE_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setName(resultSet.getString("name"));
        user.setLogin(resultSet.getString("login"));
        user.setEmail(resultSet.getString("email"));
        Date sqlDate = resultSet.getDate("birthday");
        if (sqlDate != null) {
            user.setBirthday(sqlDate.toLocalDate());
        }
        return user;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        get(friendId);
        jdbc.update(ADD_FRIEND_QUERY,
                userId,
                friendId);
    }

    @Override
    public List<User> getFriends(long userId) {
        get(userId);
        return jdbc.query(GET_FRIENDS_QUERY, this::mapRowToUser, userId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        get(userId);
        get(friendId);
        jdbc.update(DELETE_FRIEND_QUERY,
                userId,
                friendId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long userId2) {
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, this::mapRowToUser, userId, userId2);
    }

    @Override
    public boolean deleteUserById(Long userId) {
        get(userId);
        return jdbc.update(DELETE_USER_BY_ID, userId) > 0;
    }
}
