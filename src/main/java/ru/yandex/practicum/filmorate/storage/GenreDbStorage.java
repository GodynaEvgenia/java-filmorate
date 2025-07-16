package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.BaseRepository;

import java.util.List;
import java.util.Set;

@Repository
public class GenreDbStorage extends BaseRepository<Genre> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Genre findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new ResourceNotFoundException("Жанр не найден с ID: " + id));
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public void exists(Film film) {
        Set<Long> genreIds = film.getGenres();
        if (genreIds == null || genreIds.isEmpty()) return;

        for (Long genreId : genreIds) {
            Genre genre = findById(genreId);
            if (genre.getName() == null || genre.getName().isEmpty()) {
                throw new ResourceNotFoundException("Жанр с идентификатором " + genreId + " не найден.");
            }
        }
    }
}
