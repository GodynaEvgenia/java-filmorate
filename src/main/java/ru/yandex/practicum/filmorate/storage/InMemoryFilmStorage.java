package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    UserService userService;

    @Autowired
    public InMemoryFilmStorage(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public List<Film> getAll() {
        return films.values().stream().toList();
    }

    @Override
    public Film get(long filmId) {
        return Optional.ofNullable(films.get(filmId))
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с идентификатором " + filmId + " не найден"));
    }

    @Override
    public Film create(@RequestBody Film film) throws ValidationException {
        film.validate();
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(@RequestBody Film film) throws ValidationException {
        if (films.containsKey(film.getId())) {
            film.validate();
            Film filmInMemory = films.get(film.getId());
            filmInMemory.setName(film.getName());
            filmInMemory.setDescription(film.getDescription());
            filmInMemory.setReleaseDate(film.getReleaseDate());
            filmInMemory.setDuration(film.getDuration());
            return film;
        } else {
            throw new ResourceNotFoundException("Фильм с идентификатором " + film.getId() + " не найден");
        }

    }

    @Override
    public void delete(Integer filmId) {
        films.remove(filmId);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public void addLike(long id, long userId) {
        if (films.containsKey(id)) {
            if (userService.findAll().containsKey(userId)) {
                films.get(id).getLikes().add(userId);
            } else {
                throw new ResourceNotFoundException("Пользоваатель с идентификатором " + userId + " не найден");
            }

        } else {
            throw new ResourceNotFoundException("Фильм с идентификатором " + id + " не найден");
        }
    }

    @Override
    public void deleteLike(long id, long userId) {
        films.get(id).getLikes().remove(userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        List<Film> filmList = films.values().stream()
                .toList();
        List<Film> sortedFilms = filmList.stream()
                .sorted(Comparator.comparingInt(f -> f.getLikes().size()))
                .collect(Collectors.toList());
        Collections.reverse(sortedFilms);
        return sortedFilms.subList(0, Math.min(count, films.size()));
    }
}


