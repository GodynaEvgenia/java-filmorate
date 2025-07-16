package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.interfaces.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private final UserService userService;
    private final FeedService feedService;
    private final GenreService genreService;

    private final FilmStorage filmStorage;
    private final RatingStorage ratingStorage;
    private final LikesStorage likesStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    private final FilmMapper filmMapper;

    public FilmDto get(long filmId) {
        Film film = filmStorage.get(filmId);
        List<Genre> genres = genreService.getFilmGenres(filmId);
        List<Director> directors = directorStorage.getFilmDirectors(filmId);
        log.info("Получение фильмы по ID = " + filmId);
        return filmMapper.toDto(film, genres, directors);
    }

    public List<FilmDto> getAll() {
        List<Film> films = filmStorage.getAll();

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, List<Genre>> genresByFilmId = genreStorage.getGenresForFilms(filmIds);
        Map<Long, List<Director>> directorsByFilmId = directorStorage.getDirectorsForFilms(filmIds);

        log.info("Получение всех фильмов");
        return films.stream().map(film -> {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), List.of());
            List<Director> directors = directorsByFilmId.getOrDefault(film.getId(), List.of());
            return filmMapper.toDto(film, genres, directors);
        }).collect(Collectors.toList());
    }

    public FilmDto create(FilmDto filmDto) throws ValidationException {
        Film film = filmMapper.dtoToFilm(filmDto);

        film.validate();
        ratingStorage.findById(film.getRating());

        film = filmStorage.create(film);
        List<Genre> genres = genreService.getFilmGenres(film.getId());
        List<Director> directors = directorStorage.getFilmDirectors(film.getId());

        log.info("Создание нового фильма");
        return filmMapper.toDto(film, genres, directors);
    }

    public FilmDto update(FilmDto filmDto) throws ValidationException {
        Film film = filmMapper.dtoToFilm(filmDto);

        get(film.getId());

        film = filmStorage.update(film);
        List<Genre> genres = genreService.getFilmGenres(film.getId());
        List<Director> directors = directorStorage.getFilmDirectors(film.getId());

        log.info("Обновление фильма с ID = " + film.getId());
        return filmMapper.toDto(film, genres, directors);
    }

    public void addLike(long id, long userId) {
        userService.get(userId);
        likesStorage.addLike(id, userId);
        feedService.createFeed(userId, EventType.LIKE, EventOperation.ADD, id);
        log.info("Добавление лайка у фильма с ID = " + id + " пользователем с ID = " + userId);
    }

    public void deleteLike(long id, long userId) {
        userService.get(userId);
        likesStorage.deleteLike(id, userId);
        feedService.createFeed(userId, EventType.LIKE, EventOperation.REMOVE, id);
        log.info("Удаление лайка у фильма с ID = " + id + " пользователем с ID = " + userId);
    }

    public List<Film> getPopular(int count) {
        log.info("Запрос популярных фильмов");
        return filmStorage.getPopular(count);
    }

    public boolean deleteFilmById(Long id) {
        if (id == null || id < 1) throw new IllegalArgumentException("Фильм с id = " + id + " не найден");
        log.info("Удаление фильма с ID = " + id);
        return filmStorage.deleteFilmById(id);
    }

    public List<FilmDto> getPopularFilms(int count, Long genreId, Integer year) {
        if (genreId != null) {
            try {
                genreService.findById(genreId);
            } catch (ResourceNotFoundException e) {
                throw new ResourceNotFoundException("Жанр с id " + genreId + " не найден");
            }
        }

        List<Film> films;

        if (genreId == null && year == null) {
            films = getPopular(count);
        } else {
            films = filmStorage.getPopularFilmsWithFilters(count, genreId, year);
        }

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, List<Genre>> genresByFilmId = genreStorage.getGenresForFilms(filmIds);
        Map<Long, List<Director>> directorsByFilmId = directorStorage.getDirectorsForFilms(filmIds);

        return films.stream().map(film -> {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), List.of());
            List<Director> directors = directorsByFilmId.getOrDefault(film.getId(), List.of());
            return filmMapper.toDto(film, genres, directors);
        }).collect(Collectors.toList());
    }

    public List<FilmDto> getCommonFilms(long userId, long friendId) {
        userService.get(userId);
        userService.get(friendId);

        List<Film> films = filmStorage.getCommonFilms(userId, friendId);

        if (films.isEmpty()) {
            return List.of();
        }

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, List<Genre>> genresByFilmId = genreStorage.getGenresForFilms(filmIds);
        Map<Long, List<Director>> directorsByFilmId = directorStorage.getDirectorsForFilms(filmIds);

        return films.stream().map(film -> {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), List.of());
            List<Director> directors = directorsByFilmId.getOrDefault(film.getId(), List.of());
            return filmMapper.toDto(film, genres, directors);
        }).collect(Collectors.toList());

    }

    public List<FilmDto> getFilmsByDirectorSortBy(long directorId, String sortBy) {
        List<Film> films = filmStorage.getFilmsByDirectorSortBy(directorId, sortBy);
        if (films.isEmpty()) {
            throw new ResourceNotFoundException("Фильмы по Режиссеру с id = " + directorId + " не найдены");
        }
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, List<Genre>> genresByFilmId = genreStorage.getGenresForFilms(filmIds);
        Map<Long, List<Director>> directorsByFilmId = directorStorage.getDirectorsForFilms(filmIds);

        return films.stream().map(film -> {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), List.of());
            List<Director> directors = directorsByFilmId.getOrDefault(film.getId(), List.of());
            return filmMapper.toDto(film, genres, directors);
        }).collect(Collectors.toList());
    }

    public List<FilmDto> searchFilms(String query, String[] by) {
        List<Film> films = filmStorage.searchFilms(query, by);

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, List<Genre>> genresByFilmId = genreStorage.getGenresForFilms(filmIds);
        Map<Long, List<Director>> directorsByFilmId = directorStorage.getDirectorsForFilms(filmIds);

        return films.stream().map(film -> {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), List.of());
            List<Director> directors = directorsByFilmId.getOrDefault(film.getId(), List.of());
            return filmMapper.toDto(film, genres, directors);
        }).collect(Collectors.toList());
    }
}

