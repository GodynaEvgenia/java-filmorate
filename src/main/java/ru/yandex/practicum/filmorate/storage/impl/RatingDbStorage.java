package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.mapper.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.interfaces.RatingStorage;

import java.util.List;

@Component
@AllArgsConstructor
public class RatingDbStorage implements RatingStorage {

    private final JdbcTemplate jdbc;
    private final RatingRowMapper rowMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM rating";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM rating WHERE id = ?";

    @Override
    public List<Rating> findAll() {
        return jdbc.query(FIND_ALL_QUERY, rowMapper);
    }

    @Override
    public Rating findById(int id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, rowMapper, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Рейтинг MPA не найден с ID: " + id);
        }
    }
}
