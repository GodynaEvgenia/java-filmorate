package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.ReviewRatingDbStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReviewService {

    private ReviewDbStorage reviewDbStorage;
    private ReviewRatingDbStorage reviewRatingDbStorage;
    private UserService userService;
    private FilmService filmService;


    @Autowired
    public ReviewService(ReviewDbStorage reviewDbStorage, ReviewRatingDbStorage reviewRatingDbStorage,
                         UserService userService, FilmService filmService) {
        this.reviewDbStorage = reviewDbStorage;
        this.reviewRatingDbStorage = reviewRatingDbStorage;
        this.userService = userService;
        this.filmService = filmService;
    }

    public Review getById(Integer id) {
        log.debug("Запрошен отзыв с id = {}", id);
        return checkIfReviewExists(id);
    }

    public List<Review> getAll(Integer filmId, Integer count) {
        List<Review> reviews;
        if (filmId == -1) {
            log.debug("Запрошены все отзывы");
            reviews = reviewDbStorage.getAll();
        } else {
            log.debug("Запрошены все отзывы фильма с id = {}", filmId);
            reviews = reviewDbStorage.getByFilmId(filmId);
        }
        log.debug("Количество выгруженных отзывов: {}", reviews.size());
        log.trace("Перечень отзывов: {}", reviews.stream().map(Review::toString));
        if (reviews.size() > count) {
            reviews = reviews.stream().limit(count).toList();
            log.debug("Количество отзывов ограничено {}", count);
            log.trace("Итоговый перечень отзывов: {}", reviews.stream().map(Review::toString));
        }
        return reviews;
    }

    public Review add(Review filmReview) {
        log.debug("Добавление нового отзыва: {}", filmReview);
        performChecks(filmReview);
        Review review = reviewDbStorage.add(filmReview);
        log.debug("Добавлен отзыв с id = {}", review.getReviewId());
        log.trace("Итоговый отзыв: {}", review);
        return review;
    }

    public Review update(Review filmReview) {
        log.debug("Обновление отзыва: {}", filmReview);
        performChecks(filmReview);
        Review review = reviewDbStorage.update(filmReview);
        log.debug("Отзыв обновлён");
        log.trace("Итоговый отзыв: {}", review);
        return review;
    }

    public void delete(Integer id) {
        log.debug("Удаление отзыва с id = {}", id);
        Review review = checkIfReviewExists(id);
        reviewDbStorage.delete(id);
        log.debug("Удалён отзыв с id = {}", id);
    }

    public void addLikeToFilmReview(Integer reviewId, Integer userId) {
        log.debug("Добавление лайка отзыву с id = {} от пользователя с id = {}", reviewId, userId);
        changeUserReviewReaction(reviewId, userId, true);
        log.debug("Лайк добавлен");
    }

    public void addDislikeToFilmReview(Integer reviewId, Integer userId) {
        log.debug("Добавление дизлайка отзыву с id = {} от пользователя с id = {}", reviewId, userId);
        changeUserReviewReaction(reviewId, userId, false);
        log.debug("Дизлайк добавлен");
    }

    public void deleteLikeFromFilmReview(Integer reviewId, Integer userId) {
        log.debug("Удаление лайка к отзыву с id = {} от пользователя с id = {}", reviewId, userId);
        clearUserReviewReaction(reviewId, userId, true);
        log.debug("Выполнено");
    }

    public void deleteDislikeFromFilmReview(Integer reviewId, Integer userId) {
        log.debug("Удаление дизлайка к отзыву с id = {} от пользователя с id = {}", reviewId, userId);
        clearUserReviewReaction(reviewId, userId, false);
        log.debug("Выполнено");
    }

    private void changeUserReviewReaction(Integer reviewId, Integer userId, boolean isPositive) {
        User user = userService.get(userId);
        checkIfReviewExists(reviewId);
        if (isPositive) {
            reviewRatingDbStorage.addLikeToFilmReview(reviewId, user.getId());
        } else {
            reviewRatingDbStorage.addDislikeToFilmReview(reviewId, user.getId());
        }
    }

    private void clearUserReviewReaction(Integer reviewId, Integer userId, boolean isLike) {
        User user = userService.get(userId);
        checkIfReviewExists(reviewId);
        if (isLike) {
            reviewRatingDbStorage.deleteLikeFromFilmReview(reviewId, user.getId());
        } else {
            reviewRatingDbStorage.deleteDislikeFromFilmReview(reviewId, user.getId());
        }
    }

    private void performChecks(Review filmReview) {
        log.debug("Запуск проверок отзыва");
        if (filmReview.getContent() == null || filmReview.getContent().isBlank()) {
            log.warn("Ошибка проверки значений текста отзыва");
            throw new ValidationException("Текст отзыва заполнен некорректно");
        }
        if (filmReview.getUserId() == null || userService.get(filmReview.getUserId()) == null) {
            log.warn("Не обнаружен пользователь с id = {}", filmReview.getUserId());
            throw new ValidationException("Данные о пользователе заполнены некорректно");
        }
        if (filmReview.getFilmId() == null || filmService.get(filmReview.getFilmId()) == null) {
            log.warn("Не обнаружен фильм с id = {}", filmReview.getFilmId());
            throw new ValidationException("Данные о фильме заполнены некорректно");
        }
        if (filmReview.getIsPositive() == null) {
            log.warn("Ошибка проверки значения типа отзыва");
            throw new ValidationException("Данные о типе отзыва заполнены некорректно");
        }
        filmReview.setUseful(0);
        log.debug("Проверки пройдены успешно");
    }

    private Review checkIfReviewExists(Integer reviewId) {
        return Optional.ofNullable(reviewDbStorage.getById(reviewId))
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв не обнаружен"));
    }
}
