package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

@Service
public class FilmService {
    FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film get(long filmId) {
        return filmStorage.get(filmId);
    }

    public Film create(Film film) throws ValidationException {
        return filmStorage.create(film);
    }

    public Film update(Film film) throws ValidationException {
        return filmStorage.update(film);
    }

    public void addLike(long id, long userId) {
        filmStorage.addLike(id, userId);
    }

    public void deleteLike(long id, long userId) {
        filmStorage.deleteLike(id, userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }
}

