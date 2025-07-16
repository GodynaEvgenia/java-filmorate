package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.BaseRepository;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
public class DirectorDBStorage extends BaseRepository<Director> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_QUERY = "update directors set " +
            "name = ? where id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors where id = ?";

    public DirectorDBStorage(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Director findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new ResourceNotFoundException("Режиссер не найден с ID: " + id));
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

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

    public Director update(Director director) {
        Director exDirector = findById(director.getId());
        jdbc.update(UPDATE_QUERY,
                director.getName(),
                director.getId());
        return director;
    }

    public void delete(long id) {
        Director exDirector = findById(id);
        jdbc.update(DELETE_QUERY, id);
    }

}
