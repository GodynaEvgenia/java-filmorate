package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreRepository;

import java.util.List;

@Component
public class GenreDbStorage {
    GenreRepository genreRepository;

    @Autowired
    public GenreDbStorage(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Genre findById(long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Жанр не найден с ID: " + id));
    }

    public List<Genre> findAll() {
        return genreRepository.findAll();
    }
}
