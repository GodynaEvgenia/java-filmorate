package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.repository.LikesRepository;
import ru.yandex.practicum.filmorate.repository.RatingRepository;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class FilmDbStorage implements FilmStorage {
    FilmRepository filmRepository;
    RatingRepository ratingRepository;
    GenreRepository genreRepository;
    LikesRepository likesRepository;

    @Autowired
    public FilmDbStorage(FilmRepository filmRepository,
                         RatingRepository ratingRepository,
                         GenreRepository genreRepository,
                         LikesRepository likesRepository) {
        this.filmRepository = filmRepository;
        this.ratingRepository = ratingRepository;
        this.genreRepository = genreRepository;
        this.likesRepository = likesRepository;
    }

    @Override
    public Collection<Film> findAll() {
        return List.of();
    }

    @Override
    public Film get(long filmId) {
        return filmRepository.findById(filmId);
    }

    @Override
    public List<Film> getAll() {
        return filmRepository.findAll();
    }

    @Override
    public Film create(Film film) {
        film.validate();
        Rating rating = ratingRepository.findById(film.getRating())
                .orElseThrow(() -> new ResourceNotFoundException("Рейтинг не найден с ID: " + film.getRating()));
        ;
        filmRepository.save(film);

        if (film.getGenres() != null) {
            for (long genreId : film.getGenres()) {
                Genre genre = genreRepository.findById(genreId)
                        .orElseThrow(() -> new ResourceNotFoundException("Жанр не найден с ID: " + genreId));
                filmRepository.createFilmGenre(film.getId(), genreId);
            }
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        Film exFilm = filmRepository.findById(film.getId());
        return filmRepository.update(film);
    }

    @Override
    public void delete(Integer filmId) {

    }

    @Override
    public void addLike(long id, long userId) {
        likesRepository.create(id, userId);
    }

    @Override
    public void deleteLike(long id, long userId) {
        likesRepository.delete(id, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        return filmRepository.getPopular(count);
    }

    public List<Genre> getFilmGenres(long filmId) {
        return filmRepository.getFimGenres(filmId);
    }
}
