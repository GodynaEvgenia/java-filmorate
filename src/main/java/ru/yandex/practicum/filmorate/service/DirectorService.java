package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DirectorDBStorage;

import java.util.List;
import java.util.Map;


@Service
public class DirectorService {
    private DirectorDBStorage directorDbStorage;

    @Autowired
    public DirectorService(DirectorDBStorage directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }

    public Director findById(long id) {
        return directorDbStorage.findById(id);
    }

    public List<Director> findAll() {
        return directorDbStorage.findAll();
    }

    public Director create(Director director) {
        return directorDbStorage.create(director);
    }

    public Director update(Director director) {
        return directorDbStorage.update(director);
    }

    public void delete(long id) {
        directorDbStorage.delete(id);
    }

}
