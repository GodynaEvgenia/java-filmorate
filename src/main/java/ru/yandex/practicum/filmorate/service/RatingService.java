package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.interfaces.RatingStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class RatingService {

    private final RatingStorage ratingStorage;

    public Rating findById(int id) {
        log.info("Запрос рейтинга MPA с ID = " + id);
        return ratingStorage.findById(id);
    }

    public List<Rating> findAll() {
        log.info("Запрос всех рейтингов MPA");
        return ratingStorage.findAll();
    }
}
