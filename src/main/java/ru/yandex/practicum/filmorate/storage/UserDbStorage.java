package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.BaseRepository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String INSERT_QUERY = "INSERT INTO users(name, login, email, birthday) VALUES (?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String UPDATE_QUERY = "update users set " +
            "name = ?, login = ?, email = ?, birthday = ? " +
            "where id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
    private static final String GET_FRIENDS_QUERY = "" +
            "SELECT * from users u, friendship f where f.user_id = ? and u.id = f.friend_id";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendship where user_id = ? and friend_id = ?";
    private static final String GET_COMMON_FRIENDS_QUERY = "" +
            "SELECT * from users u where u.id in (" +
            "SELECT f1.friend_id " +
            "FROM friendship f1 " +
            "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
            "WHERE f1.user_id = ?" +
            "  AND f2.user_id = ?)";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Map<Long, User> findAll() {
        return Map.of();
    }

    @Override
    public List<User> getAll() {
        return jdbc.query(FIND_ALL_QUERY, this::mapRowToUser);
    }

    @Override
    public User get(long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользоваатель не найден с ID: " + id));
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
            stmt.setString(4, null);
            return stmt;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        User exUser = get(user.getId());
        jdbc.update(UPDATE_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public void delete(Integer userId) {
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

    public void addFriend(long userId, long friendId) {
        User exFriend = get(friendId);
        jdbc.update(ADD_FRIEND_QUERY,
                userId,
                friendId);
    }

    public List<User> getFriends(long userId) {
        User exUser = get(userId);
        return jdbc.query(GET_FRIENDS_QUERY, this::mapRowToUser, userId);
    }

    public void deleteFriend(long userId, long friendId) {
        User exUser = get(userId);
        User exFriend = get(friendId);
        jdbc.update(DELETE_FRIEND_QUERY,
                userId,
                friendId);
    }

    public List<User> getCommonFriends(long userId, long userId2) {
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, this::mapRowToUser, userId, userId2);
    }

    @Override
    public boolean deleteUserById(Long userId) {
        User exUser = get(userId);
        List<User> friends = jdbc.query(GET_FRIENDS_QUERY, this::mapRowToUser, userId);

        // Теперь удаляем каждого друга из базы данных
        for (User friend : friends) {
            deleteFriend(userId, friend.getId());
        }
        String sql = "DELETE FROM users WHERE id = ?";

        return jdbc.update(sql, userId) > 0;
    }

    public Optional<User> findUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbc.queryForObject(sql, this::mapRowToUser, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
