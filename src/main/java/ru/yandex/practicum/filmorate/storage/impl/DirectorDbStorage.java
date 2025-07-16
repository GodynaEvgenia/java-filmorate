package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.interfaces.DirectorStorage;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper rowMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_QUERY = "update directors set " +
            "name = ? where id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors where id = ?";
    private static final String GET_DIRECTORS_FOR_FILMS_QUERY = """
            SELECT fd.film_id, d.id, d.name
            FROM film_director fd
            JOIN directors d ON fd.director_id = d.id
            WHERE fd.film_id IN (%s)
            ORDER BY fd.film_id, d.id
            """;
    private static final String GET_FILM_DIRECTORS_QUERY =
            "SELECT d.id, d.name " +
                    "FROM film_director fd " +
                    "JOIN directors d ON d.id = fd.director_id " +
                    "WHERE film_id = ?" +
                    "ORDER BY id";

    @Override
    public Director findById(long id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, rowMapper, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Режиссер не найден с ID: " + id);
        }
    }

    @Override
    public List<Director> findAll() {
        return jdbc.query(FIND_ALL_QUERY, rowMapper);
    }

    @Override
    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY, new String[]{"id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        director.setId(keyHolder.getKey().longValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        findById(director.getId());
        jdbc.update(UPDATE_QUERY,
                director.getName(),
                director.getId());
        return director;
    }

    @Override
    public void delete(long id) {
        findById(id);
        jdbc.update(DELETE_QUERY, id);
    }

    @Override
    public List<Director> getFilmDirectors(long filmId) {
        return jdbc.query(GET_FILM_DIRECTORS_QUERY, rowMapper, filmId);
    }

    @Override
    public Map<Long, List<Director>> getDirectorsForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }

        String inClause = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        String sql = String.format(GET_DIRECTORS_FOR_FILMS_QUERY, inClause);

        return jdbc.query(sql, rs -> {
            Map<Long, List<Director>> result = new HashMap<>();
            int rowNum = 1;
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Director director = rowMapper.mapRow(rs, rowNum);
                result.computeIfAbsent(filmId, k -> new ArrayList<>()).add(director);
                rowNum++;
            }
            return result;
        });
    }

}
