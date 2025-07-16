package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@ControllerAdvice
@AllArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        return genreService.findById(id);
    }

    @GetMapping
    public List<Genre> findAll() {
        return genreService.findAll();
    }
}