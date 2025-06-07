package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserStorage {
    Collection<User> findAll();

    User get(long userId);

    List<User> getAll();

    User create(User user);

    User update(User user);

    void delete(Integer userId);

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    Set<User> getFriends(long userId);

    Set<User> getCommonFriends(long id, long otherId);
}
