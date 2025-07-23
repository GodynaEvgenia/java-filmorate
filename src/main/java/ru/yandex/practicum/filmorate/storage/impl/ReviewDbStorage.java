package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.interfaces.ReviewStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbc;
    private final ReviewRowMapper rowMapper;

    private static final String GET_REVIEW_BASE_QUERY =
            "SELECT review_id, film_id, user_id, useful, is_positive, content FROM reviews ";
    private static final String GET_ALL_REVIEWS = GET_REVIEW_BASE_QUERY + " ORDER BY useful DESC";
    private static final String GET_REVIEW_BY_ID_QUERY =
            GET_REVIEW_BASE_QUERY + " WHERE review_id = ?";
    private static final String GET_REVIEW_BY_FILM_ID_QUERY =
            GET_REVIEW_BASE_QUERY + " WHERE film_id = ? ORDER BY useful DESC";
    private static final String SAVE_REVIEW_QUERY =
            "INSERT INTO reviews (film_id, user_id, useful, is_positive, content) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_REVIEW_QUERY =
            "UPDATE reviews SET useful = ?, is_positive = ?, content = ? WHERE review_id = ?";
    private static final String DELETE_REVIEW_BY_ID_QUERY =
            "DELETE FROM reviews WHERE review_id = ?";

    @Override
    public Review add(Review review) {
        KeyHolder holder = new GeneratedKeyHolder();

        jdbc.update(conn -> {
            PreparedStatement smt = conn.prepareStatement(SAVE_REVIEW_QUERY, new String[]{"review_id"});
            smt.setLong(1, review.getFilmId());
            smt.setLong(2, review.getUserId());
            smt.setInt(3, review.getUseful());
            smt.setBoolean(4, review.getIsPositive());
            smt.setString(5, review.getContent());
            return smt;
        }, holder);

        int reviewId = Objects.requireNonNull(holder.getKey()).intValue();
        return getById(reviewId);
    }

    @Override
    public Review update(Review filmReview) {
        jdbc.update(
                UPDATE_REVIEW_QUERY,
                filmReview.getUseful(),
                filmReview.getIsPositive(),
                filmReview.getContent(),
                filmReview.getReviewId()
        );
        return getById(filmReview.getReviewId());
    }

    @Override
    public void delete(Integer id) {
        jdbc.update(DELETE_REVIEW_BY_ID_QUERY, id);
    }

    @Override
    public Review getById(Integer id) {
        try {
            return jdbc.queryForObject(GET_REVIEW_BY_ID_QUERY, rowMapper, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Отзыв не найден с ID: " + id);
        }
    }

    @Override
    public List<Review> getByFilmId(Integer id) {
        return jdbc.query(GET_REVIEW_BY_FILM_ID_QUERY, rowMapper, id);
    }

    @Override
    public List<Review> getAll() {
        return jdbc.query(GET_ALL_REVIEWS, rowMapper);
    }
}
