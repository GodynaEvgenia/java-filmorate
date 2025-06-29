package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.LikeMapper;
import ru.yandex.practicum.filmorate.model.Like;


@Repository
public class LikesRepository extends BaseRepository<Like> {
    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? and user_id = ?";

    public LikesRepository(JdbcTemplate jdbc, LikeMapper mapper) {
        super(jdbc, mapper);
    }

    public void create(long filmId, long userId) {
        jdbc.update(INSERT_LIKE_QUERY,
                filmId,
                userId);
    }

    public void delete(long filmId, long userId) {
        jdbc.update(DELETE_LIKE_QUERY,
                filmId,
                userId);
    }
}
