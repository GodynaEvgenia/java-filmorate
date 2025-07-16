package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public Genre findById(long id) {
        log.info("Запрос жанра с ID = " + id);
        return genreStorage.findById(id);
    }

    public List<Genre> findAll() {
        log.info("Запрос всех жанров");
        return genreStorage.findAll();
    }

    public List<Genre> getFilmGenres(long filmId) {
        log.info("Запрос жанров по фильму с ID = " + filmId);
        return genreStorage.getFilmGenres(filmId);
    }
}
