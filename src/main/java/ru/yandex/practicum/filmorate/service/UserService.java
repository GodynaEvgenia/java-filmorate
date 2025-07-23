package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.interfaces.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final FeedService feedService;

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    private final FilmMapper filmMapper;

    public User get(long id) {
        return userStorage.get(id);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
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

    public List<FilmDto> getRecommendations(Long userId) {
        User user = get(userId);

        Set<Long> filmsWithLikeByUser = new HashSet<>(filmStorage.findFilmLikes(user));

        List<User> otherUsers = getAll().stream()                       //
                .filter(u -> !u.getId().equals(userId))
                .toList();

        Map<User, Integer> similarityMap = new HashMap<>();

        Map<Long, List<Long>> otherUserWithFilmIdsMap = filmStorage.findFilmLikesMap(otherUsers);

        for (User otherUser : otherUsers) {
            if (otherUserWithFilmIdsMap.get(otherUser.getId()) != null) {
                Set<Long> filmsWithLikeByOtherUser = new HashSet<>(otherUserWithFilmIdsMap.get(otherUser.getId()));
                Set<Long> filmsWithLikeByUserCopy = new HashSet<>(filmsWithLikeByUser);
                //ищем совпадение по ИД фильмов
                filmsWithLikeByUserCopy.retainAll(filmsWithLikeByOtherUser);
                //сохраняем количество совпадений у другого пользователя в мапу
                similarityMap.put(otherUser, filmsWithLikeByUserCopy.size());
            }
        }

        if (similarityMap.isEmpty()) {
            return Collections.emptyList();
        }

        //ищем максимальное количество совпадений
        int maxSimilarity = similarityMap.values().stream().max(Integer::compareTo).orElse(0);

        if (maxSimilarity == 0) {
            return Collections.emptyList();
        }

        //Находим пользователей с таким количеством совпадений
        List<User> mostSimilarUsers = similarityMap.entrySet().stream()
                .filter(e -> e.getValue() == maxSimilarity)
                .map(Map.Entry::getKey)
                .toList();

        Map<Long, List<Long>> userWithFilmIdsMap = filmStorage.findFilmLikesMap(mostSimilarUsers);
        List<Long> recommendedFilmIds = new ArrayList<>();
        for (User similarUser : mostSimilarUsers) {
            if (userWithFilmIdsMap.get(similarUser.getId()) != null) {
                Set<Long> filmIds = new HashSet<>(userWithFilmIdsMap.get(similarUser.getId()));
                //Удаляем повторения с основным пользователем
                filmIds.removeAll(filmsWithLikeByUser);
                recommendedFilmIds.addAll(filmIds);
            }
        }
        recommendedFilmIds = recommendedFilmIds.stream().distinct().toList();

        Map<Long, List<Genre>> genresByFilmId = genreStorage.getGenresForFilms(recommendedFilmIds);
        Map<Long, List<Director>> directorsByFilmId = directorStorage.getDirectorsForFilms(recommendedFilmIds);

        List<Long> finalRecommendedFilmIds = recommendedFilmIds;
        return filmStorage.getAll().stream()
                .filter(film -> finalRecommendedFilmIds.contains(film.getId()))
                .map(film -> {
                    List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), List.of());
                    List<Director> directors = directorsByFilmId.getOrDefault(film.getId(), List.of());
                    return filmMapper.toDto(film, genres, directors);
                }).collect(Collectors.toList());
    }
}
