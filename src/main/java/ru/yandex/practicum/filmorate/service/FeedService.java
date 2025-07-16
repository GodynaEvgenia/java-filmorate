package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.interfaces.FeedStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public List<Feed> getFeed(int userId) {
        userStorage.get(userId);
        log.info("Запрос ленты событий у пользователя с ID = " + userId);
        return feedStorage.getFeed(userId);
    }

    public void createFeed(long userId, EventType eventType, EventOperation eventOperation, long entityId) {
        Feed feed = new Feed();
        feed.setTimestamp(Instant.now().toEpochMilli());
        feed.setUserId(userId);
        feed.setEventType(eventType);
        feed.setOperation(eventOperation);
        feed.setEntityId(entityId);
        log.info("Создание события " + eventOperation.name() + " " + eventType.name() +
                " у пользователя с ID = " + userId);
        feedStorage.createEvent(feed);
    }
}
