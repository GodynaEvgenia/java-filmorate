package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.LikesRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@Repository
public class ReviewDbStorage {

    private static final String GET_REVIEW_BASE_QUERY =
            "SELECT review_id, film_id, user_id, useful, is_positive, content FROM reviews ";
    private static final String GET_ALL_REVIEWS = GET_REVIEW_BASE_QUERY + " ORDER BY useful DESC";
    private static final String GET_REVIEW_BY_ID_QUERY =
            GET_REVIEW_BASE_QUERY + " WHERE review_id = ?";
    private static final String GET_REVIEW_BY_FILM_ID_QUERY =
            GET_REVIEW_BASE_QUERY + " WHERE film_id = ? ORDER BY useful DESC";
    private static final String SAVE_REVIEW_QUERY =
            "INSERT INTO reviews (film_id, user_id, useful, is_positive, content) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_REVIEW_QUERY =
            "UPDATE reviews SET useful = ?, is_positive = ?, content = ? WHERE review_id = ?";
    private static final String DELETE_REVIEW_BY_ID_QUERY =
            "DELETE FROM reviews WHERE review_id = ?";
    private static final String CHECK_REVIEW_EXISTS_BY_ID_QUERY =
            "SELECT EXISTS(SELECT review_id FROM reviews WHERE review_id = ?) isExists";
    private final JdbcTemplate jdbc;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Review add(Review review) {
        KeyHolder holder = new GeneratedKeyHolder();

        jdbc.update(conn -> {
            PreparedStatement smt = conn.prepareStatement(SAVE_REVIEW_QUERY, new String[]{"review_id"});
            smt.setLong(1, review.getFilmId());
            smt.setLong(2, review.getUserId());
            smt.setInt(3, review.getUseful());
            smt.setBoolean(4, review.getIsPositive());
            smt.setString(5, review.getContent());
            return smt;
        }, holder);

        int reviewId = Objects.requireNonNull(holder.getKey()).intValue();
        return getById(reviewId);
    }

    public Review update(Review filmReview) {
        jdbc.update(
                UPDATE_REVIEW_QUERY,
                filmReview.getUseful(),
                filmReview.getIsPositive(),
                filmReview.getContent(),
                filmReview.getReviewId()
        );
        return getById(filmReview.getReviewId());
    }

    public void delete(Integer id) {
        jdbc.update(DELETE_REVIEW_BY_ID_QUERY, id);
    }

    public Review getById(Integer id) {
        return jdbc.query(GET_REVIEW_BY_ID_QUERY, this::mapRowToReview, id)
                .stream()
                .findAny()
                .orElse(null);
    }

    public List<Review> getByFilmId(Integer id) {
        return jdbc.query(GET_REVIEW_BY_FILM_ID_QUERY, this::mapRowToReview, id);
    }

    public List<Review> getAll() {
        return jdbc.query(GET_ALL_REVIEWS, this::mapRowToReview);
    }

    public boolean isExists(Integer id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                CHECK_REVIEW_EXISTS_BY_ID_QUERY, (rs, rowNum) -> rs.getBoolean("isExists"), id
        ));
    }

    public Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }
}
