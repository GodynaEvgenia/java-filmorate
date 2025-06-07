package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> findAll();

    Film get(long filmId);

    List<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    void delete(Integer filmId);

    void addLike(long id, long userId);

    void deleteLike(long id, long userId);

    List<Film> getPopular(int count);
}
