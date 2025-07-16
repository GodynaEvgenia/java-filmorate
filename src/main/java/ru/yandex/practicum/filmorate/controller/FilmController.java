package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/films")
@ControllerAdvice
@AllArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping()
    public List<FilmDto> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{filmId}")
    public FilmDto findById(@PathVariable long filmId) {
        return filmService.get(filmId);
    }

    @PostMapping()
    public FilmDto create(@RequestBody FilmDto filmDto) {
        return filmService.create(filmDto);
    }

    @PutMapping()
    public FilmDto update(@Valid @RequestBody FilmDto filmDto) {
        return filmService.update(filmDto);
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
    public List<FilmDto> getPopularFilms(
            @RequestParam(value = "count", defaultValue = "10") int count,
            @RequestParam(value = "genreId", required = false)
                @Positive(message = "Genre ID must be positive") Long genreId,
            @RequestParam(value = "year", required = false)
                @Min(value = 1900, message = "Year must be no earlier than 1900")
                @Max(value = 2100, message = "Year must be no later than 2100") Integer year) {
        log.info("Request for popular films: count={}, genreId={}, year={}", count, genreId, year);
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<FilmDto> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{id}")
    public boolean deleteFilmById(@PathVariable Long id) {
        return filmService.deleteFilmById(id);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getFilmsByDirectorSortBy(
            @PathVariable long directorId,
            @RequestParam(required = false) String sortBy) {
        return filmService.getFilmsByDirectorSortBy(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<FilmDto> searchFilms(
            @RequestParam("query") String query,
            @RequestParam(value = "by", required = false) String[] by) {
        log.info("Параметры: query " + query);
        log.info("Параметры: by " + Arrays.toString(by));
        return filmService.searchFilms(query, by);
    }
}
