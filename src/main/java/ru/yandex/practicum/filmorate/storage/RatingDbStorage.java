package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.repository.RatingRepository;

import java.util.List;

@Component
public class RatingDbStorage {
    RatingRepository ratingRepository;

    @Autowired
    public RatingDbStorage(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public Rating findById(int id) {
        return ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Рейтинг не найден с ID: " + id));
    }

    public List<Rating> findAll() {
        return ratingRepository.findAll();
    }
}
