package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String INSERT_QUERY =
            "INSERT INTO films(name, description, release_date, duration, rating) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating = ? WHERE id = ?";
    private static final String INSERT_GENRY_QUERY =
            "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String GET_POPULAR_QUERY =
            "SELECT films.id, name, description, release_date, duration, rating, COUNT(likes.id) AS likes_count " +
                    "FROM films " +
                    "LEFT JOIN likes " +
                    "ON films.id = likes.film_id " +
                    "GROUP BY films.id " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";
    private static final String GET_POPULAR_WITH_FILTERS_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating, COUNT(l.id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN likes l " +
                    "ON f.id = l.film_id " +
                    "LEFT JOIN film_genre fg " +
                    "ON f.id = fg.film_id " +
                    "WHERE (? IS NULL OR fg.genre_id = ?) " +
                    "AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?) " +
                    "GROUP BY f.id " +
                    "ORDER BY likes_count " +
                    "DESC LIMIT ?";
    private static final String GET_COMMON_FILMS_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating, COUNT(l.id) AS likes_count " +
                    "FROM films f " +
                    "JOIN likes l " +
                    "ON f.id = l.film_id " +
                    "WHERE f.id IN ( " +
                        "SELECT film_id " +
                        "FROM likes " +
                        "WHERE user_id = ? " +
                        "INTERSECT " +
                        "SELECT film_id " +
                        "FROM likes " +
                        "WHERE user_id = ?) " +
                    "GROUP BY f.id " +
                    "ORDER BY likes_count DESC";
    private static final String INSERT_FILM_DIRECTOR_QUERY =
            "INSERT INTO film_director(film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS_QUERY =
            "DELETE FROM film_director WHERE film_id = ?";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String GET_FILMS_BY_DIRECTOR_SORT_BY_LIKES_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, rating, COUNT(l.id) AS likes_count, d.name " +
            "FROM films f " +
            "JOIN film_director fd ON f.id = fd.film_id " +
            "LEFT JOIN likes l ON f.id = l.film_id " +
            "JOIN directors d on d.id = fd.director_id " +
            "WHERE fd.director_id = ? " +
            "GROUP BY f.id " +
            "ORDER BY likes_count DESC ";
    private static final String GET_FILMS_BY_DIRECTOR_SORT_BY_YEAR_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating, d.name " +
            "FROM films f " +
            "JOIN film_director fd ON f.id = fd.film_id " +
            "JOIN directors d on d.id = fd.director_id " +
            "WHERE fd.director_id = ? " +
            "ORDER BY YEAR(f.release_date)";
    private static final String DELETE_FILM_BY_ID = "DELETE FROM films WHERE id = ?";
    private static final String GET_FILM_BY_USER_ID_LIKE =
            "SELECT film_id FROM likes WHERE user_id = ?";
    private static final String GET_FILMS_BY_USER_ID_LIKES =
                """
                SELECT film_id, user_id
                FROM likes
                WHERE user_id IN (%s)
                """;


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
        int rating = resultSet.getInt("rating");
        if (rating > 0) {
            film.setRating(rating);
        }
        return film;
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

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null) {
            List<Object[]> batchArgs = new ArrayList<>();
            for (long genreId : film.getGenres()) {
                batchArgs.add(new Object[]{film.getId(), genreId});
            }
            try {
                jdbc.batchUpdate(INSERT_GENRY_QUERY, batchArgs);
            } catch (DataIntegrityViolationException exception) {
                throw new ResourceNotFoundException("Некорректные жанры");
            }
        }

        if (film.getDirectors() != null) {
            List<Object[]> batchArgs = new ArrayList<>();
            for (Director d : film.getDirectors()) {
                batchArgs.add(new Object[]{film.getId(), d.getId()});
            }
            try {
                jdbc.batchUpdate(INSERT_FILM_DIRECTOR_QUERY, batchArgs);
            } catch (DataIntegrityViolationException exception) {
                throw new ResourceNotFoundException("Некорректные режиссеры");
            }
        }

        return film;
    }

    @Override
    public Film update(Film film) {

        jdbc.update(UPDATE_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getRating(), film.getId());

        if (film.getGenres() != null) {
            jdbc.update(DELETE_FILM_GENRES_QUERY, film.getId());
            List<Object[]> batchArgs = new ArrayList<>();
            for (long genreId : film.getGenres()) {
                batchArgs.add(new Object[]{film.getId(), genreId});
            }
            jdbc.batchUpdate(INSERT_GENRY_QUERY, batchArgs);
        }

        if (film.getDirectors() != null) {

            jdbc.update(DELETE_FILM_DIRECTORS_QUERY, film.getId());
            List<Object[]> batchArgs = new ArrayList<>();
            for (Director d : film.getDirectors()) {
                batchArgs.add(new Object[]{film.getId(), d.getId()});
            }
            jdbc.batchUpdate(INSERT_FILM_DIRECTOR_QUERY, batchArgs);
        }

        return film;
    }

    @Override
    public List<Film> getPopular(int count) {
        return jdbc.query(GET_POPULAR_QUERY, this::mapRowToFilm, count);
    }

    @Override
    public boolean deleteFilmById(Long filmId) {
        return jdbc.update(DELETE_FILM_BY_ID, filmId) > 0;
    }

    @Override
    public List<Film> getPopularFilmsWithFilters(int count, Long genreId, Integer year) {
        return jdbc.query(GET_POPULAR_WITH_FILTERS_QUERY, this::mapRowToFilm, genreId, genreId, year, year, count);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return jdbc.query(GET_COMMON_FILMS_QUERY, this::mapRowToFilm, userId, friendId);
    }

    @Override
    public List<Film> getFilmsByDirectorSortBy(long directorId, String sortBy) {
        if (sortBy.equals("year")) {
            return jdbc.query(GET_FILMS_BY_DIRECTOR_SORT_BY_YEAR_QUERY, this::mapRowToFilm, directorId);
        } else {
            return jdbc.query(GET_FILMS_BY_DIRECTOR_SORT_BY_LIKES_QUERY, this::mapRowToFilm, directorId);
        }
    }

    @Override
    public Set<Long> findFilmLikes(User user) {
        return new HashSet<>(jdbc.queryForList(GET_FILM_BY_USER_ID_LIKE, Long.class, user.getId()));
    }

    @Override
    public Map<Long, List<Long>> findFilmLikesMap(List<User> users) {

        String inClause = users.stream()
                .map(User::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = String.format(GET_FILMS_BY_USER_ID_LIKES, inClause);

        return jdbc.query(sql, rs -> {
            Map<Long, List<Long>> userIdWithFilmIdsMap = new HashMap<>();
            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                Long filmId = rs.getLong("film_id");
                if (userIdWithFilmIdsMap.containsKey(userId)) {
                    userIdWithFilmIdsMap.get(userId).add(filmId);
                } else {
                    userIdWithFilmIdsMap.put(userId, new ArrayList<>(Arrays.asList(filmId)));
                }
            }
            return userIdWithFilmIdsMap;
        });
    }

    private String generateQuery(String[] by) {
        String q = "SELECT f.id, f.name, f.description, f.release_date, f.duration, rating," +
                "COUNT(l.id) AS likes_count, d.name " +
                "FROM films f " +
                "LEFT JOIN film_director fd ON f.id = fd.film_id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "LEFT JOIN directors d on d.id = fd.director_id ";
        if (by.length == 2) {
            q = q + " WHERE f.name ilike ? or d.name ilike ? ";
        } else {
            if (Arrays.asList(by).contains("title")) {
                q = q + " WHERE f.name ilike ? ";
            } else {
                q = q + " WHERE d.name ilike ? ";
            }
        }
        q = q + "GROUP BY f.id " +
                "ORDER BY likes_count DESC ";
        return q;
    }

    @Override
    public List<Film> searchFilms(String query, String[] by) {
        String querySql = generateQuery(by);
        log.info(querySql);
        if (by.length == 2) {
            return jdbc.query(querySql, this::mapRowToFilm, "%" + query + "%", "%" + query + "%");
        } else {
            return jdbc.query(querySql, this::mapRowToFilm, "%" + query + "%");
        }

    }

}
