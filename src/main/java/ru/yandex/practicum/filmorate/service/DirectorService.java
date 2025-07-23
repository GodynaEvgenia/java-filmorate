package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.interfaces.DirectorStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director findById(long id) {
        log.info("Запрос режиссера по ID = " + id);
        return directorStorage.findById(id);
    }

    public List<Director> findAll() {
        log.info("Запрос всех режиссеров");
        return directorStorage.findAll();
    }

    public Director create(Director director) {
        log.info("Создание нового режиссера");
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        log.info("Обновление режиссера с ID = " + director.getId());
        return directorStorage.update(director);
    }

    public void delete(long id) {
        log.info("Удаление режиссера с ID = " + id);
        directorStorage.delete(id);
    }

}
