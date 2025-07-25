package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@ControllerAdvice
@AllArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping()
    public List<Director> getAll() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable int id) {
        return directorService.findById(id);
    }

    @PostMapping()
    public Director create(@Valid @RequestBody Director director) {
        director = directorService.create(director);
        return director;
    }

    @PutMapping()
    public Director update(@Valid @RequestBody Director director) {
        return directorService.update(director);
    }

    @DeleteMapping("/{id}") //пользователь удаляет лайк.
    public void delete(@PathVariable long id) {
        directorService.delete(id);
    }

}
