package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.RatingService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/films")
@ControllerAdvice
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
        FilmDto filmDto = mapper.toDto(film);
        return filmDto;
    }

    @PostMapping()
    public FilmDto create(@RequestBody FilmDto filmDto) {
        Film film = mapper.dtoToFilm(filmDto);
        film = filmService.create(film);
        FilmDto result = mapper.toDto(film);
        return result;
    }

    @PutMapping()
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
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
    public List<FilmDto> getPopularFilms(@RequestParam(value = "count", defaultValue = "10") int count) {
        List<Film> films = filmService.getPopular(count);
        List<FilmDto> listFilmDto = new ArrayList<>();
        for (Film film : films) {
            listFilmDto.add(mapper.toDto(film));
        }
        return listFilmDto;
    }

    @DeleteMapping("/{id}")
    public boolean deleteFilmById(@PathVariable Long id) {
        return filmService.deleteFilmById(id);
    }
}
