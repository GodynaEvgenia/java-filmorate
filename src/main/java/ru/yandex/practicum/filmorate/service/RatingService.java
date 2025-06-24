package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.util.List;

@Service
public class RatingService {
    RatingDbStorage ratingDbStorage;

    @Autowired
    public RatingService(RatingDbStorage genreDbStorage) {
        this.ratingDbStorage = genreDbStorage;
    }

    public Rating findById(int id){
        return ratingDbStorage.findById(id);
    }

    public List<Rating> findAll(){
        return ratingDbStorage.findAll();
    }
}
