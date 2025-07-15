package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {
    private UserDbStorage userStorage;
    private FeedService feedService;
    private final FilmStorage filmStorage;

    @Autowired
    public UserService(UserDbStorage userStorage, FeedService feedService, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.feedService = feedService;
        this.filmStorage = filmStorage;
    }

    public User get(long id) {
        return userStorage.get(id);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public Map<Long, User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void addFriend(long userId, long friendId) {
        userStorage.addFriend(userId, friendId);
        feedService.createFeed(userId, EventType.FRIEND, EventOperation.ADD, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        userStorage.deleteFriend(userId, friendId);
        feedService.createFeed(userId, EventType.FRIEND, EventOperation.REMOVE, friendId);
    }

    public List<User> getFriends(long userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(long id, long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public boolean deleteUserById(Long userId) {
        if (userId == null || userId < 1) throw new IllegalArgumentException("Invalid User id");
        return userStorage.deleteUserById(userId);
    }

    public User getUserById(Long id) {
        return userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public List<Film> getRecommendations(Long userId) {
        User targetUser = getUserById(userId);

        Set<Long> targetLikes = new HashSet<>(filmStorage.findFilmLikes(targetUser));

        Collection<User> allUsers = getAll().stream()                       //
                .filter(u -> !u.getId().equals(userId))
                .toList();

        Map<User, Integer> similarityMap = new HashMap<>();

        for (User otherUser : allUsers) {
            Set<Long> otherLikes = new HashSet<>(filmStorage.findFilmLikes(otherUser));
            Set<Long> intersection = new HashSet<>(targetLikes);
            intersection.retainAll(otherLikes);
            similarityMap.put(otherUser, intersection.size());              //
        }

        if (similarityMap.isEmpty()) {
            return Collections.emptyList();
        }

        int maxSimilarity = similarityMap.values().stream().max(Integer::compareTo).orElse(0);

        if (maxSimilarity == 0) {
            return Collections.emptyList();                             //
        }

        List<User> mostSimilarUsers = similarityMap.entrySet().stream()
                .filter(e -> e.getValue() == maxSimilarity)
                .map(Map.Entry::getKey)
                .toList();                                          //

        Set<Long> recommendedFilmIds = new HashSet<>();
        for (User similarUser : mostSimilarUsers) {
            Set<Long> likes = filmStorage.findFilmLikes(similarUser);
            likes.removeAll(targetLikes); // Только те, которых нет у target
            recommendedFilmIds.addAll(likes);
        }

        return recommendedFilmIds.stream()
                .map(filmStorage::findFilmById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
