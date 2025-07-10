package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@ControllerAdvice
public class DirectorController {
    private final DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping()
    public List<Director> getAll() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable int id) {
        return directorService.findById(id);
    }

    @PostMapping()
    public Director create(@RequestBody Director director) {
        //Director dire = mapper.dtoToFilm(filmDto);
        director = directorService.create(director);
        //Director result = mapper.toDto(film);
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
