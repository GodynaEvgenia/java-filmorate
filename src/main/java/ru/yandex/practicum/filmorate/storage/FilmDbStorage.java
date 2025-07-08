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
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String UPDATE_QUERY = "update films set " +
            "name = ?, description = ?, release_date = ?, duration = ?, rating = ? " +
            "where id = ?";
    private static final String INSERT_GENRY_QUERY = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String GET_POPULAR_QUERY = "" +
            "SELECT films.id, name, description, release_date, duration, rating, COUNT(likes.id) AS likes_count " +
            "FROM films " +
            "JOIN likes ON films.id = likes.film_id " +
            "GROUP BY films.name " +
            "ORDER BY likes_count DESC " +
            "LIMIT ?";
    private static final String GET_FILM_GENRES_QUERY = "" +
            "SELECT g.id, g.name, g.description " +
            "FROM film_genre fg " +
            "JOIN genre g ON g.id = fg.genre_id " +
            "WHERE film_id = ?" +
            "ORDER BY id";

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc,
                         RatingDbStorage ratingDbStorage,
                         GenreDbStorage genreDbStorage,
                         LikesRepository likesRepository) {
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

    /*public void createFilmGenre(long filmId, long genreId) {
        jdbc.update(INSERT_GENRY_QUERY,
                filmId,
                genreId);
    }*/

    @Override
    public Film update(Film film) {
        Film exFilm = get(film.getId());
        jdbc.update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating(),
                film.getId());
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
}
