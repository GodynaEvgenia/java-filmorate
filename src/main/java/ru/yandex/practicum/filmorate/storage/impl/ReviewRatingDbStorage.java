package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.interfaces.ReviewRatingStorage;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@AllArgsConstructor
public class ReviewRatingDbStorage implements ReviewRatingStorage {

    private final JdbcTemplate jdbc;

    private static final String MERGE_REVIEW_RATING_QUERY =
            "MERGE INTO review_ratings (review_id, user_id, is_positive) VALUES (?, ?, ?)";
    private static final String DELETE_REVIEW_RATING_BY_REVIEW_ID_USER_ID =
            "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ?";
    private static final String GET_REVIEW_USEFUL_SCORE_ON_REVIEW_ID =
            "SELECT SUM(CASE WHEN is_positive = TRUE THEN 1 ELSE -1 END) useful FROM review_ratings WHERE review_id = ?";
    private static final String UPDATE_REVIEW_USEFUL_SCORE_ON_USEFUL_SCORE_REVIEW_ID =
            "UPDATE reviews SET useful = ? WHERE review_id = ?";

    @Override
    public void addLikeToFilmReview(Integer reviewId, Long userId) {
        addUserReviewRating(reviewId, userId, Boolean.TRUE);
    }

    @Override
    public void addDislikeToFilmReview(Integer reviewId, Long userId) {
        addUserReviewRating(reviewId, userId, Boolean.FALSE);
    }

    @Override
    public void deleteLikeFromFilmReview(Integer reviewId, Long userId) {
        deleteUserReviewRating(reviewId, userId);
    }

    @Override
    public void deleteDislikeFromFilmReview(Integer reviewId, Long userId) {
        deleteUserReviewRating(reviewId, userId);
    }

    @Override
    public void addUserReviewRating(Integer reviewId, Long userId, Boolean isPositive) {
        jdbc.update(MERGE_REVIEW_RATING_QUERY, reviewId, userId, isPositive);
        updateReviewUsefulScore(reviewId);
    }

    @Override
    public void deleteUserReviewRating(Integer reviewId, Long userId) {
        jdbc.update(DELETE_REVIEW_RATING_BY_REVIEW_ID_USER_ID, reviewId, userId);
        updateReviewUsefulScore(reviewId);
    }

    @Override
    public void updateReviewUsefulScore(Integer reviewId) {
        Integer usefulScore = getReviewUsefulScore(reviewId);
        jdbc.update(UPDATE_REVIEW_USEFUL_SCORE_ON_USEFUL_SCORE_REVIEW_ID, usefulScore, reviewId);
    }

    @Override
    public Integer getReviewUsefulScore(Integer reviewId) {
        return jdbc.query(GET_REVIEW_USEFUL_SCORE_ON_REVIEW_ID, this::mapRow, reviewId)
                .stream()
                .findAny()
                .orElse(null);
    }

    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("useful");
    }
}
