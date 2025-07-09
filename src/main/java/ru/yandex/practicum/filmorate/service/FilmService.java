package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.util.List;

@Service
public class FilmService {
    private FilmDbStorage filmStorage;
    private UserService userService;

    @Autowired
    public FilmService(FilmDbStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film get(long filmId) {
        return filmStorage.get(filmId);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
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

    public List<Genre> getFilmGenres(long filmId) {
        return filmStorage.getFilmGenres(filmId);
    }

    public List<Film> getPopularFilmsWithFilters(int count, long genreId, Integer year) {
        return filmStorage.getPopularFilmsWithFilters(count, genreId, year);
    }
}

