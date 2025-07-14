package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private FilmDbStorage filmStorage;
    private UserService userService;
    private final FilmMapper filmMapper;
    private final GenreService genreService;

    @Autowired
    public FilmService(FilmDbStorage filmStorage, UserService userService, FilmMapper filmMapper, GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.filmMapper = filmMapper;
        this.genreService = genreService;
    }

    public FilmDto get(long filmId) {
        Film film = filmStorage.get(filmId);
        List<Genre> genres = getFilmGenres(filmId);
        List<Director> directors = getFilmDirectors(filmId);
        return filmMapper.toDto(film, genres, directors);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public FilmDto create(FilmDto filmDto) throws ValidationException {
        Film film = filmMapper.dtoToFilm(filmDto);
        film = filmStorage.create(film);
        List<Genre> genres = getFilmGenres(film.getId());
        List<Director> directors = getFilmDirectors(film.getId());
        return filmMapper.toDto(film, genres, directors);
    }

    public FilmDto update(FilmDto filmDto) throws ValidationException {
        Film film = filmMapper.dtoToFilm(filmDto);
        film = filmStorage.update(film);
        List<Genre> genres = getFilmGenres(film.getId());
        List<Director> directors = getFilmDirectors(film.getId());
        return filmMapper.toDto(film, genres, directors);
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

        return films.stream().map(film -> {
            List<Genre> genres = getFilmGenres(film.getId());
            List<Director> directors = getFilmDirectors(film.getId());
            return filmMapper.toDto(film, genres, directors);
        }).collect(Collectors.toList());
    }

    public List<Film> getPopularFilmsWithFilters(int count, Long genreId, Integer year) {
        return filmStorage.getPopularFilmsWithFilters(count, genreId, year);
    }

    public List<FilmDto> getCommonFilms(long userId, long friendId) {
        userService.get(userId);
        userService.get(friendId);

        List<Film> films = filmStorage.getCommonFilms(userId, friendId);

        if (films.isEmpty()) {
            return List.of();
        }

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, List<Genre>> genresByFilmId = filmStorage.getGenresForFilms(filmIds);
        Map<Long, List<Director>> directorsByFilmId = filmStorage.getDirectorsForFilms(filmIds);

        return films.stream().map(film -> {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), List.of());
            List<Director> directors = directorsByFilmId.getOrDefault(film.getId(), List.of());
            return filmMapper.toDto(film, genres, directors);
        }).collect(Collectors.toList());

    }

    public List<FilmDto> getFilmsByDirectorSortBy(long directorId, String sortBy) {
        List<Film> films = filmStorage.getFilmsByDirectorSortBy(directorId, sortBy);
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, List<Genre>> genresByFilmId = filmStorage.getGenresForFilms(filmIds);
        Map<Long, List<Director>> directorsByFilmId = filmStorage.getDirectorsForFilms(filmIds);

        return films.stream().map(film -> {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), List.of());
            List<Director> directors = directorsByFilmId.getOrDefault(film.getId(), List.of());
            return filmMapper.toDto(film, genres, directors);
        }).collect(Collectors.toList());
    }

    public List<Director> getFilmDirectors(long filmId) {
        return filmStorage.getFilmDirectors(filmId);
    }

}

