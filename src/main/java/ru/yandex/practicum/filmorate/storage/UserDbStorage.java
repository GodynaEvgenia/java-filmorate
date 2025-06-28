package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserDbStorage implements UserStorage {
    UserRepository userRepository;

    @Autowired
    public UserDbStorage(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<Long, User> findAll() {
        return Map.of();
    }

    @Override
    public User get(long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User create(User user) {
        user.validate();
        userRepository.save(user);
        return user;
    }

    @Override
    public User update(User user) {
        User exUser = userRepository.findById(user.getId());
        return userRepository.update(user);
    }

    @Override
    public void delete(Integer userId) {

    }

    @Override
    public void addFriend(long userId, long friendId) {
        User exFriend = userRepository.findById(friendId);
        userRepository.addFriend(userId, friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        User exUser = userRepository.findById(userId);
        User exFriend = userRepository.findById(friendId);
        userRepository.deleteFriend(userId, friendId);
    }

    @Override
    public List<User> getFriends(long userId) {
        User exUser = userRepository.findById(userId);
        return userRepository.getFriends(userId);
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        return userRepository.getCommonFriends(id, otherId);
    }
}
