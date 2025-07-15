package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class FeedService {

    private FeedDbStorage feedDbStorage;
    private UserDbStorage userDbStorage;

    @Autowired
    public FeedService(FeedDbStorage feedDbStorage, UserDbStorage userDbStorage) {
        this.feedDbStorage = feedDbStorage;
        this.userDbStorage = userDbStorage;
    }

    public List<Feed> getFeed(int userId) {
        Optional.ofNullable(userDbStorage.get(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Пользователя с id=" + userId + " не существует"));
        return feedDbStorage.getFeed(userId);
    }

    public void createFeed(long userId, EventType eventType, EventOperation eventOperation, long entityId) {
        Feed feed = new Feed();
        feed.setTimestamp(Instant.now().toEpochMilli());
        feed.setUserId(userId);
        feed.setEventType(eventType);
        feed.setOperation(eventOperation);
        feed.setEntityId(entityId);

        feedDbStorage.createEvent(feed);
    }
}
