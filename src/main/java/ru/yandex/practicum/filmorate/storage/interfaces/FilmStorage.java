package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {

    Film get(long id);

    List<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    List<Film> getPopular(int count);

    boolean deleteFilmById(Long id);

    List<Film> getPopularFilmsWithFilters(int count, Long genreId, Integer year);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getFilmsByDirectorSortBy(long directorId, String sortBy);

    Set<Long> findFilmLikes(User user);

    Map<Long, List<Long>> findFilmLikesMap(List<User> users);

    List<Film> searchFilms(String query, String[] by);
}