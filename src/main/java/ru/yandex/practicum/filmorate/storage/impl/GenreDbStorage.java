package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;
    private final GenreRowMapper rowMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM genre";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE id = ?";
    private static final String GET_FILM_GENRES_QUERY =
            "SELECT g.id, g.name, g.description " +
                    "FROM film_genre fg " +
                    "JOIN genre g " +
                    "ON g.id = fg.genre_id " +
                    "WHERE film_id = ?" +
                    "ORDER BY id";
    private static final String GET_GENRES_FOR_FILMS_QUERY = """
            SELECT fg.film_id, g.id, g.name, g.description
            FROM film_genre fg
            JOIN genre g ON fg.genre_id = g.id
            WHERE fg.film_id IN (%s)
            ORDER BY fg.film_id, g.id
            """;

    @Override
    public Genre findById(long id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, rowMapper, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Жанр не найден с ID: " + id);
        }
    }

    @Override
    public List<Genre> findAll() {
        return jdbc.query(FIND_ALL_QUERY, rowMapper);
    }

    @Override
    public List<Genre> getFilmGenres(long filmId) {
        return jdbc.query(GET_FILM_GENRES_QUERY, rowMapper, filmId);
    }

    @Override
    public Map<Long, List<Genre>> getGenresForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }

        String inClause = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        String sql = String.format(GET_GENRES_FOR_FILMS_QUERY, inClause);

        return jdbc.query(sql, rs -> {
            Map<Long, List<Genre>> result = new HashMap<>();
            int rowNum = 1;
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = rowMapper.mapRow(rs, rowNum); // Используем новый метод
                result.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
                rowNum++;
            }
            return result;
        });
    }

}
