package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

public interface FilmStorage {
    Collection<Film> findAll();

    Film get(long id);

    List<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    void delete(Integer filmId);

    void addLike(long id, long userId);

    void deleteLike(long id, long userId);

    List<Film> getPopular(int count);

    boolean deleteFilmById(Long id);

    List<Film> getPopularFilmsWithFilters(int count, Long genreId, Integer year);

    List<Film> getCommonFilms(Long userId, Long friendId);

    Map<Long, List<Genre>> getGenresForFilms(List<Long> filmIds);

    public Set<Long> findFilmLikes(User user);

    public Optional<Film> findFilmById(Long filmId);
}