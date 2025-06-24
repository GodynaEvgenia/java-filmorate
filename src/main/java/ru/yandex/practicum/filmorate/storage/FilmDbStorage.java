package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public class FilmDbStorage implements FilmStorage{
    @Override
    public Collection<Film> findAll() {
        return List.of();
    }

    @Override
    public Film get(long filmId) {
        return null;
    }

    @Override
    public List<Film> getAll() {
        return List.of();
    }

    @Override
    public Film create(Film film) {
        return null;
    }

    @Override
    public Film update(Film film) {
        return null;
    }

    @Override
    public void delete(Integer filmId) {

    }

    @Override
    public void addLike(long id, long userId) {

    }

    @Override
    public void deleteLike(long id, long userId) {

    }

    @Override
    public List<Film> getPopular(int count) {
        return List.of();
    }
}
