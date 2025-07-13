package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.repository.LikesRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    protected final JdbcTemplate jdbc;
    private RatingDbStorage ratingDbStorage;
    private GenreDbStorage genreDbStorage;
    private LikesRepository likesRepository;

    private static final String FIND_ALL_QUERY = "select * from films";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating) " + "VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String UPDATE_QUERY = "update films set " + "name = ?, description = ?, release_date = ?, duration = ?, rating = ? " + "where id = ?";
    private static final String INSERT_GENRY_QUERY = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String GET_POPULAR_QUERY = "" + "SELECT films.id, name, description, release_date, duration, rating, COUNT(likes.id) AS likes_count " + "FROM films " + "JOIN likes ON films.id = likes.film_id " + "GROUP BY films.name " + "ORDER BY likes_count DESC " + "LIMIT ?";
    private static final String GET_FILM_GENRES_QUERY = "" + "SELECT g.id, g.name, g.description " + "FROM film_genre fg " + "JOIN genre g ON g.id = fg.genre_id " + "WHERE film_id = ?" + "ORDER BY id";
    private static final String GET_POPULAR_WITH_FILTERS_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating, COUNT(l.id) AS likes_count FROM films f LEFT JOIN likes l ON f.id = l.film_id LEFT JOIN film_genre fg ON f.id = fg.film_id WHERE (? IS NULL OR fg.genre_id = ?) AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?) GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";
    private static final String GET_COMMON_FILMS_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating, COUNT(l.id) AS likes_count " + "FROM films f " + "JOIN likes l ON f.id = l.film_id " + "WHERE f.id IN ( " + "   SELECT film_id FROM likes WHERE user_id = ? " + "   INTERSECT " + "   SELECT film_id FROM likes WHERE user_id = ? " + ") " + "GROUP BY f.id " + "ORDER BY likes_count DESC";

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, RatingDbStorage ratingDbStorage, GenreDbStorage genreDbStorage, LikesRepository likesRepository) {
        this.jdbc = jdbc;
        this.ratingDbStorage = ratingDbStorage;
        this.genreDbStorage = genreDbStorage;
        this.likesRepository = likesRepository;
    }

    public Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        Date sqlDate = resultSet.getDate("release_date");
        film.setDuration(resultSet.getInt("duration"));
        if (sqlDate != null) {
            film.setReleaseDate(sqlDate.toLocalDate());
        }
        film.setRating(resultSet.getInt("rating"));
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return List.of();
    }

    @Override
    public Film get(long id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, this::mapRowToFilm, id);
        } catch (EmptyResultDataAccessException ignored) {
            throw new ResourceNotFoundException("Фильм не найден с ID: " + id);
        }
    }

    @Override
    public List<Film> getAll() {
        return jdbc.query(FIND_ALL_QUERY, this::mapRowToFilm);
    }

    @Override
    public Film create(Film film) {
        film.validate();
        Rating rating = ratingDbStorage.findById(film.getRating());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            Date sqlDate = Date.valueOf(film.getReleaseDate());
            stmt.setDate(3, sqlDate);
            stmt.setLong(4, film.getDuration());
            if (film.getRating() == null) {
                stmt.setNull(5, Types.BIGINT);
            } else {
                stmt.setLong(5, film.getRating());
            }
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        if (film.getGenres() != null) {
            List<Object[]> batchArgs = new ArrayList<>();
            for (long genreId : film.getGenres()) {
                Genre genre = genreDbStorage.findById(genreId);
                batchArgs.add(new Object[]{film.getId(), genreId});
            }
            int[] updateCounts = jdbc.batchUpdate(INSERT_GENRY_QUERY, batchArgs);
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        Film exFilm = get(film.getId());
        jdbc.update(UPDATE_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getRating(), film.getId());
        return film;
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
        return jdbc.query(GET_POPULAR_QUERY, this::mapRowToFilm, count);
    }

    public List<Genre> getFilmGenres(long filmId) {
        return jdbc.query(GET_FILM_GENRES_QUERY, this::mapRowToGenre, filmId);
    }


    public List<Genre> getFimGenres(long filmId) {
        return jdbc.query(GET_FILM_GENRES_QUERY, this::mapRowToGenre, filmId);
    }

    public Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getLong("id"));
        genre.setName(resultSet.getString("name"));
        genre.setDescription(resultSet.getString("description"));
        return genre;
    }

    public List<Film> getPopularFilmsWithFilters(int count, Long genreId, Integer year) {
        return jdbc.query(GET_POPULAR_WITH_FILTERS_QUERY, this::mapRowToFilm, genreId, genreId, year, year, count);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return jdbc.query(GET_COMMON_FILMS_QUERY, this::mapRowToFilm, userId, friendId);
    }
}
