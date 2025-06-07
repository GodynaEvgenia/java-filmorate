package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User get(long userId) {
        return Optional.ofNullable(users.get(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с идентификатором " + userId + " не найден"));
    }

    @Override
    public List<User> getAll() {
        return users.values().stream().toList();/*List<User> userList = new ArrayList<>();
        for (User user : users) {
            userList.add(user);
        }
        return userList;*/
    }

    @Override
    public User create(User user) {
        user.validate();
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            user.validate();
            User userInMemory = users.get(user.getId());
            userInMemory.setEmail(user.getEmail());
            userInMemory.setLogin(user.getLogin());
            if (user.getName() == null || user.getName().isBlank()) {
                userInMemory.setName(user.getLogin());
            } else {
                userInMemory.setName(user.getName());
            }
            userInMemory.setBirthday(user.getBirthday());

            return user;
        } else {
            throw new ResourceNotFoundException("Пользователь с идентификатором " + user.getId() + " не найден");
        }
    }

    @Override
    public void delete(Integer userId) {
        users.remove(userId);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        User user = users.get(userId);
        user.getFriends().add(friendId);
        User friend = get(friendId);
        friend.getFriends().add(userId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        User user = get(userId);
        user.getFriends().remove(friendId);
        User friend = get(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public Set<User> getFriends(long userId) {
        if (users.containsKey(userId)) {
            Set<User> friendsSet = new HashSet<>();
            Set<Long> friends = users.get(userId).getFriends();
            for (Long friendId : friends) {
                friendsSet.add(get(friendId));
            }
            return friendsSet;
        } else {
            throw new ResourceNotFoundException("Пользователь с идентификатором " + userId + " не найден");
        }

    }

    @Override
    public Set<User> getCommonFriends(long id, long otherId) {
        Set<User> commonFriendsSet = new HashSet<>();
        Set<Long> userFriends = users.get(id).getFriends();
        Set<Long> otherUserFriends = users.get(otherId).getFriends();
        userFriends.retainAll(otherUserFriends);
        for (Long userId : userFriends) {
            commonFriendsSet.add(users.get(userId));
        }
        return commonFriendsSet;
    }
}
