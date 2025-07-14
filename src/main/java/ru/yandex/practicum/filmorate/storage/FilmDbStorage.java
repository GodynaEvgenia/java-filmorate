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
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.repository.LikesRepository;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    protected final JdbcTemplate jdbc;
    private RatingDbStorage ratingDbStorage;
    private GenreDbStorage genreDbStorage;
    private LikesRepository likesRepository;
    private DirectorDBStorage directorDBStorage;

    private static final String FIND_ALL_QUERY = "select * from films";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating) " + "VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String UPDATE_QUERY = "update films set " + "name = ?, description = ?, release_date = ?, duration = ?, rating = ? " + "where id = ?";
    private static final String INSERT_GENRY_QUERY = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String GET_POPULAR_QUERY = "" + "SELECT films.id, name, description, release_date, duration, rating, COUNT(likes.id) AS likes_count " + "FROM films " + "left JOIN likes ON films.id = likes.film_id " + "GROUP BY films.id " + "ORDER BY likes_count DESC " + "LIMIT ?";
    private static final String GET_FILM_GENRES_QUERY = "" + "SELECT g.id, g.name, g.description " + "FROM film_genre fg " + "JOIN genre g ON g.id = fg.genre_id " + "WHERE film_id = ?" + "ORDER BY id";
    private static final String GET_POPULAR_WITH_FILTERS_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating, COUNT(l.id) AS likes_count FROM films f LEFT JOIN likes l ON f.id = l.film_id LEFT JOIN film_genre fg ON f.id = fg.film_id WHERE (? IS NULL OR fg.genre_id = ?) AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?) GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";
    private static final String GET_COMMON_FILMS_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating, COUNT(l.id) AS likes_count " + "FROM films f " + "JOIN likes l ON f.id = l.film_id " + "WHERE f.id IN ( " + "   SELECT film_id FROM likes WHERE user_id = ? " + "   INTERSECT " + "   SELECT film_id FROM likes WHERE user_id = ? " + ") " + "GROUP BY f.id " + "ORDER BY likes_count DESC";
    private static final String GET_GENRES_FOR_FILMS_QUERY = """
            SELECT fg.film_id, g.id, g.name, g.description
            FROM film_genre fg
            JOIN genre g ON fg.genre_id = g.id
            WHERE fg.film_id IN (%s)
            ORDER BY fg.film_id, g.id
            """;
    private static final String GET_DIRECTORS_FOR_FILMS_QUERY = """
            SELECT fd.film_id, d.id, d.name
            FROM film_director fd
            JOIN directors d ON fd.director_id = d.id
            WHERE fd.film_id IN (%s)
            ORDER BY fd.film_id, d.id
            """;
    private static final String GET_FILM_DIRECTORS_QUERY = "" +
            "SELECT d.id, d.name " +
            "FROM film_director fd " +
            "JOIN directors d ON d.id = fd.director_id " +
            "WHERE film_id = ?" +
            "ORDER BY id";
    private static final String INSERT_FILM_DIRECTOR_QUERY = "INSERT INTO film_director(film_id, director_id) VALUES " +
            " (?, ?)";
    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM film_director WHERE film_id = ?";
    private static final String GET_FILMS_BY_DIRECTOR_SORT_BY_LIKES_QUERY = "" +
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, rating," +
            "COUNT(l.id) AS likes_count, d.name " +
            "FROM films f " +
            "JOIN film_director fd ON f.id = fd.film_id " +
            "LEFT JOIN likes l ON f.id = l.film_id " +
            "JOIN directors d on d.id = fd.director_id " +
            "WHERE fd.director_id = ? " +
            "GROUP BY f.id " +
            "ORDER BY likes_count DESC ";
    private static final String GET_FILMS_BY_DIRECTOR_SORT_BY_YEAR_QUERY = "" +
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating, d.name " +
            "FROM films f " +
            "JOIN film_director fd ON f.id = fd.film_id " +
            "JOIN directors d on d.id = fd.director_id " +
            "WHERE fd.director_id = ? " +
            "ORDER BY YEAR(f.release_date)";


    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc,
                         RatingDbStorage ratingDbStorage,
                         GenreDbStorage genreDbStorage,
                         LikesRepository likesRepository,
                         DirectorDBStorage directorDBStorage) {
        this.jdbc = jdbc;
        this.ratingDbStorage = ratingDbStorage;
        this.genreDbStorage = genreDbStorage;
        this.likesRepository = likesRepository;
        this.directorDBStorage = directorDBStorage;
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
        int rating = resultSet.getInt("rating");
        if (rating > 0) {
            film.setRating(rating);
        }
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
    public Optional<Film> findFilmById(Long filmId) {
        String sql = "SELECT f.*, r.id, r.name as mpa_name " +
                "FROM films f " +
                "JOIN rating r ON f.rating = r.id " +
                "WHERE f.id = ?";
        try {
            Film film = jdbc.queryForObject(sql, this::mapRowToFilm, filmId);
            if (film != null) {
                loadFilmGenres(film);
                loadFilmLikes(film);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
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

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null) {
            List<Object[]> batchArgs = new ArrayList<>();
            for (long genreId : film.getGenres()) {
                Genre genre = genreDbStorage.findById(genreId);
                batchArgs.add(new Object[]{film.getId(), genreId});
            }
            int[] updateCounts = jdbc.batchUpdate(INSERT_GENRY_QUERY, batchArgs);
        }

        if (film.getDirectors() != null) {
            List<Object[]> batchArgs = new ArrayList<>();
            for (Director d : film.getDirectors()) {
                Director director = directorDBStorage.findById(d.getId());
                batchArgs.add(new Object[]{film.getId(), d.getId()});
            }
            int[] updateCounts = jdbc.batchUpdate(INSERT_FILM_DIRECTOR_QUERY, batchArgs);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        Film exFilm = get(film.getId());
        jdbc.update(UPDATE_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getRating(), film.getId());
        if (film.getDirectors() != null) {

            jdbc.update(DELETE_FILM_DIRECTORS_QUERY, film.getId());
            List<Object[]> batchArgs = new ArrayList<>();
            for (Director d : film.getDirectors()) {
                Director director = directorDBStorage.findById(d.getId());
                batchArgs.add(new Object[]{film.getId(), d.getId()});
            }
            int[] updateCounts = jdbc.batchUpdate(INSERT_FILM_DIRECTOR_QUERY, batchArgs);
        }

        if (film.getDirectors() != null) {

            jdbc.update(DELETE_FILM_DIRECTORS_QUERY, film.getId());
            List<Object[]> batchArgs = new ArrayList<>();
            for (Director d : film.getDirectors()) {
                Director director = directorDBStorage.findById(d.getId());
                batchArgs.add(new Object[]{film.getId(), d.getId()});
            }
            int[] updateCounts = jdbc.batchUpdate(INSERT_FILM_DIRECTOR_QUERY, batchArgs);
        }

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

    @Override
    public boolean deleteFilmById(Long filmId) {
        String sql = "DELETE FROM films WHERE id = ?";
        return jdbc.update(sql, filmId) > 0;
    }

    public List<Film> getPopularFilmsWithFilters(int count, Long genreId, Integer year) {
        return jdbc.query(GET_POPULAR_WITH_FILTERS_QUERY, this::mapRowToFilm, genreId, genreId, year, year, count);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return jdbc.query(GET_COMMON_FILMS_QUERY, this::mapRowToFilm, userId, friendId);
    }

    private Genre mapToGenre(ResultSet rs) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("name"));
        genre.setDescription(rs.getString("description"));
        return genre;
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
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = mapToGenre(rs); // Используем новый метод
                result.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
            }
            return result;
        });
    }

    private Director mapToDirector(ResultSet rs) throws SQLException {
        Director director = new Director();
        director.setId(rs.getLong("id"));
        director.setName(rs.getString("name"));

        return director;
    }

    public Map<Long, List<Director>> getDirectorsForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }

        String inClause = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        String sql = String.format(GET_DIRECTORS_FOR_FILMS_QUERY, inClause);

        return jdbc.query(sql, rs -> {
            Map<Long, List<Director>> result = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Director director = mapToDirector(rs);
                result.computeIfAbsent(filmId, k -> new ArrayList<>()).add(director);
            }
            return result;
        });
    }

    public Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {
        Director director = new Director();
        director.setId(resultSet.getLong("id"));
        director.setName(resultSet.getString("name"));

        return director;
    }

    public List<Director> getFilmDirectors(long filmId) {
        return jdbc.query(GET_FILM_DIRECTORS_QUERY, this::mapRowToDirector, filmId);
    }

    public List<Film> getFilmsByDirectorSortBy(long directorId, String sortBy) {
        if (sortBy.equals("year")) {
            return jdbc.query(GET_FILMS_BY_DIRECTOR_SORT_BY_YEAR_QUERY, this::mapRowToFilm, directorId);
        } else {
            return jdbc.query(GET_FILMS_BY_DIRECTOR_SORT_BY_LIKES_QUERY, this::mapRowToFilm, directorId);
        }
    }


    private void loadFilmLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbc.queryForList(sql, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));
    }

    @Override
    public Set<Long> findFilmLikes(User user) {
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        return new HashSet<>(jdbc.queryForList(sql, Long.class, user.getId()));
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT g.id, g.name " +
                "FROM genre g " +
                "JOIN film_genre fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";

        List<Genre> genres = jdbc.query(sql, (resultSet, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(resultSet.getLong("id"));
            genre.setName(resultSet.getString("name"));
            return genre;
        }, film.getId());

        Set<Long> genreIds = genres.stream()
                .map(Genre::getId) // Получаем идентификаторы жанров
                .collect(Collectors.toSet()); // Собираем их в Set

        film.setGenres(genreIds);
    }
}
