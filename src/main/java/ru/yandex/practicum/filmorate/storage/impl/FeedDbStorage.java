package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.FeedRowMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.interfaces.FeedStorage;

import java.util.List;

@Component
@AllArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbc;
    private final FeedRowMapper rowMapper;

    private static final String FIND_BY_USER_ID_QUERY = "SELECT * FROM feed WHERE user_id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO feed (user_id, timestamp, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";

    @Override
    public List<Feed> getFeed(int userId) {
        return jdbc.query(FIND_BY_USER_ID_QUERY, rowMapper, userId);
    }

    @Override
    public void createEvent(Feed feed) {
        jdbc.update(
                INSERT_QUERY,
                feed.getUserId(),
                feed.getTimestamp(),
                feed.getEventType().name(),
                feed.getOperation().name(),
                feed.getEntityId()
        );
    }

}
