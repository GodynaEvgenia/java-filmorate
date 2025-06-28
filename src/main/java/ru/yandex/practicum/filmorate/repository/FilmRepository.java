package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.*;
import java.util.List;


@Slf4j
@Repository
public class FilmRepository extends BaseRepository<Film> {
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

    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Film> findAll() {
        return jdbc.query(FIND_ALL_QUERY, this::mapRowToFilm);
    }

    public Film findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм не найден с ID: " + id));
    }

    public Film save(Film film) {
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
        return film;
    }

    public Film update(Film film) {
        jdbc.update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating(),
                film.getId());
        return film;
    }

    public Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        Date sqlDate = resultSet.getDate("release_date");
        if (sqlDate != null) {
            film.setReleaseDate(sqlDate.toLocalDate());
        }
        film.setRating(resultSet.getInt("rating"));
        return film;
    }

    public void createFilmGenre(long filmId, long genreId) {
        jdbc.update(INSERT_GENRY_QUERY,
                filmId,
                genreId);
    }

    public List<Film> getPopular(int count) {
        return jdbc.query(GET_POPULAR_QUERY, this::mapRowToFilm, count);
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
