package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.interfaces.LikesStorage;

@Component
@AllArgsConstructor
public class LikesDbStorage implements LikesStorage {
    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? and user_id = ?";

    private final JdbcTemplate jdbc;

    @Override
    public void addLike(long filmId, long userId) {
        jdbc.update(INSERT_LIKE_QUERY,
                filmId,
                userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        jdbc.update(DELETE_LIKE_QUERY,
                filmId,
                userId);
    }

}
