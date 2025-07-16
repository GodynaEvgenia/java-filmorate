package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class FeedDbStorage {

    private final JdbcTemplate jdbc;

    public FeedDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Feed> getFeed(int userId) {
        final String sql = "SELECT * " + "FROM feed " + "WHERE user_id = ? ";

        return jdbc.query(sql, this::mapRowFeed, userId);
    }

    public void createEvent(Feed feed) {
        final String sql = "insert into feed (user_id, timestamp, event_type, operation, entity_id) values (?, ?, ?, ?, ?)";

        jdbc.update(sql, feed.getUserId(), feed.getTimestamp(), feed.getEventType().name(), feed.getOperation().name(), feed.getEntityId());
    }

    public Feed mapRowFeed(ResultSet rs, Integer rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEventId(rs.getInt("event_id"));
        feed.setEventType(EventType.valueOf(rs.getString("event_type")));
        feed.setOperation(EventOperation.valueOf(rs.getString("operation")));
        feed.setTimestamp(rs.getLong("timestamp"));
        feed.setUserId(rs.getLong("user_id"));
        feed.setEntityId(rs.getLong("entity_id"));
        return feed;
    }
}
