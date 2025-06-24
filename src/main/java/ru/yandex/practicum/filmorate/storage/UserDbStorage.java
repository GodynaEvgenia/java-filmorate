package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;
@Component
public class UserDbStorage implements UserStorage{
    @Override
    public Map<Long, User> findAll() {
        return Map.of();
    }

    @Override
    public User get(long userId) {
        return null;
    }

    @Override
    public List<User> getAll() {
        return List.of();
    }

    @Override
    public User create(User user) {
        return null;
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public void delete(Integer userId) {

    }

    @Override
    public void addFriend(long userId, long friendId) {

    }

    @Override
    public void deleteFriend(long userId, long friendId) {

    }

    @Override
    public Set<User> getFriends(long userId) {
        return Set.of();
    }

    @Override
    public Set<User> getCommonFriends(long id, long otherId) {
        return Set.of();
    }
}
