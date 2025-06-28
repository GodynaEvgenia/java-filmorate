package ru.yandex.practicum.filmorate.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.LinkedHashSet;
import java.util.List;

@Component
public class FilmMapper {
    private final RatingService ratingService;
    private final GenreService genreService;
    private final FilmService filmService;

    @Autowired
    public FilmMapper(RatingService ratingService,
                      GenreService genreService,
                      FilmService filmService){
        this.ratingService = ratingService;
        this.genreService = genreService;
        this.filmService = filmService;
    }
    public FilmDto toDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        Rating mpa = new Rating();
        if (film.getRating() != null) {
            mpa = ratingService.findById(film.getRating());
        }
        UniObject uo = new UniObject();
        uo.setId(mpa.getId());
        uo.setName(mpa.getName());
        dto.setMpa(uo);
        List<Genre> genres = filmService.getFilmGenres(film.getId());
        dto.setGenres(new LinkedHashSet<>(genres));

        return dto;
    }

    public Film dtoToFilm(FilmDto filmDto) {
        Film film = new Film();
        film.setName(filmDto.getName());
        film.setDescription(filmDto.getDescription());
        film.setReleaseDate(filmDto.getReleaseDate());
        film.setDuration(filmDto.getDuration());
        film.setRating(filmDto.getMpa().getId());

        film.setGenres(new LinkedHashSet<>());
        if (filmDto.getGenres() != null) {
            for (Genre genre : filmDto.getGenres()) {
                film.getGenres().add(genre.getId());
            }
        }
        return film;
    }
}
