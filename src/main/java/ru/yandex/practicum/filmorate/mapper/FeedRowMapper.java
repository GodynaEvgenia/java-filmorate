package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {
    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
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
