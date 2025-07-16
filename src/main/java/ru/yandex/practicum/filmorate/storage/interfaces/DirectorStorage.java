package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;

public interface DirectorStorage {
    Director findById(long id);

    List<Director> findAll();

    Director create(Director director);

    Director update(Director director);

    void delete(long id);

    List<Director> getFilmDirectors(long filmId);

    Map<Long, List<Director>> getDirectorsForFilms(List<Long> filmIds);
}
