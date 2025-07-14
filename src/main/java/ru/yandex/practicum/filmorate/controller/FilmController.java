package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.RatingService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/films")
@ControllerAdvice
@Validated
@Slf4j
public class FilmController {
    private final FilmService filmService;
    private final UserService userService;
    private final RatingService ratingService;
    private final GenreService genreService;
    private final FilmMapper mapper;

    @Autowired
    public FilmController(FilmService filmService,
                          UserService userService,
                          RatingService ratingService,
                          GenreService genreService,
                          FilmMapper filmMapper) {
        this.filmService = filmService;
        this.userService = userService;
        this.ratingService = ratingService;
        this.genreService = genreService;
        this.mapper = filmMapper;
    }

    @GetMapping()
    public List<Film> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{filmId}")
    public FilmDto findById(@PathVariable long filmId) {
        Film film = filmService.get(filmId);
        List<Genre> genres = filmService.getFilmGenres(filmId);
        return mapper.toDto(film, genres);
    }

    @PostMapping()
    public FilmDto create(@RequestBody FilmDto filmDto) {
        Film film = mapper.dtoToFilm(filmDto);
        film = filmService.create(film);
        List<Genre> genres = filmService.getFilmGenres(film.getId());
        return mapper.toDto(film, genres);
    }

    @PutMapping()
    public FilmDto update(@Valid @RequestBody FilmDto filmDto) {
        Film film = mapper.dtoToFilm(filmDto);
        film = filmService.update(film);
        FilmDto result = mapper.toDto(film);
        return result;
    }

    @PutMapping("/{id}/like/{userId}") //пользователь ставит лайк фильму.
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}") //пользователь удаляет лайк.
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(@RequestParam(value = "count", defaultValue = "10") int count,

                                         @RequestParam(value = "genreId", required = false) @Positive(message = "Genre ID must be positive") Long genreId, @RequestParam(value = "year", required = false) @Min(value = 1900, message = "Year must be no earlier than 1900") @Max(value = 2100, message = "Year must be no later than 2100") Integer year) {
        log.info("Request for popular films: count={}, genreId={}, year={}", count, genreId, year);
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<FilmDto> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }
}
