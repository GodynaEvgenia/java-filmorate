package ru.yandex.practicum.filmorate.storage.interfaces;

public interface ReviewRatingStorage {
    void addLikeToFilmReview(Integer reviewId, Long userId);

    void addDislikeToFilmReview(Integer reviewId, Long userId);

    void deleteLikeFromFilmReview(Integer reviewId, Long userId);

    void deleteDislikeFromFilmReview(Integer reviewId, Long userId);

    void addUserReviewRating(Integer reviewId, Long userId, Boolean isPositive);

    void deleteUserReviewRating(Integer reviewId, Long userId);

    void updateReviewUsefulScore(Integer reviewId);

    Integer getReviewUsefulScore(Integer reviewId);
}
