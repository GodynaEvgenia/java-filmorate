package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;

public interface GenreStorage {
    Genre findById(long id);

    List<Genre> findAll();

    List<Genre> getFilmGenres(long filmId);

    Map<Long, List<Genre>> getGenresForFilms(List<Long> filmIds);
}
