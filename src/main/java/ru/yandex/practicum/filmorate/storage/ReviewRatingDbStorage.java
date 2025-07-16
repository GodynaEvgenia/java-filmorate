package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Repository
public class ReviewRatingDbStorage {

    private static final String MERGE_REVIEW_RATING_QUERY =
            "MERGE INTO review_ratings (review_id, user_id, is_positive) VALUES (?, ?, ?)";
    private static final String DELETE_REVIEW_RATING_BY_REVIEW_ID_USER_ID =
            "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ?";
    private static final String GET_REVIEW_USEFUL_SCORE_ON_REVIEW_ID =
            "SELECT SUM(CASE WHEN is_positive = TRUE THEN 1 ELSE -1 END) useful FROM review_ratings WHERE review_id = ?";
    private static final String UPDATE_REVIEW_USEFUL_SCORE_ON_USEFUL_SCORE_REVIEW_ID =
            "UPDATE reviews SET useful = ? WHERE review_id = ?";
    private final JdbcTemplate jdbc;

    @Autowired
    public ReviewRatingDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void addLikeToFilmReview(Integer reviewId, Long userId) {
        addUserReviewRating(reviewId, userId, Boolean.TRUE);
    }

    public void addDislikeToFilmReview(Integer reviewId, Long userId) {
        addUserReviewRating(reviewId, userId, Boolean.FALSE);
    }

    public void deleteLikeFromFilmReview(Integer reviewId, Long userId) {
        deleteUserReviewRating(reviewId, userId);
    }

    public void deleteDislikeFromFilmReview(Integer reviewId, Long userId) {
        deleteUserReviewRating(reviewId, userId);
    }

    private void addUserReviewRating(Integer reviewId, Long userId, Boolean isPositive) {
        jdbc.update(MERGE_REVIEW_RATING_QUERY, reviewId, userId, isPositive);
        updateReviewUsefulScore(reviewId);
    }

    private void deleteUserReviewRating(Integer reviewId, Long userId) {
        jdbc.update(DELETE_REVIEW_RATING_BY_REVIEW_ID_USER_ID, reviewId, userId);
        updateReviewUsefulScore(reviewId);
    }

    private void updateReviewUsefulScore(Integer reviewId) {
        Integer usefulScore = getReviewUsefulScore(reviewId);
        jdbc.update(UPDATE_REVIEW_USEFUL_SCORE_ON_USEFUL_SCORE_REVIEW_ID, usefulScore, reviewId);
    }

    private Integer getReviewUsefulScore(Integer reviewId) {
        return jdbc.query(GET_REVIEW_USEFUL_SCORE_ON_REVIEW_ID, this::mapRow, reviewId)
                .stream()
                .findAny()
                .orElse(null);
    }

    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("useful");
    }
}
