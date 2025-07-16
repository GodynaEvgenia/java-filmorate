package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User get(long userId);

    List<User> getAll();

    User create(User user);

    User update(User user);

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    List<User> getFriends(long userId);

    List<User> getCommonFriends(long id, long otherId);

    boolean deleteUserById(Long id);
}
