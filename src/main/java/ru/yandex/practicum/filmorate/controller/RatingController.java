package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@ControllerAdvice
@AllArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @GetMapping("/{id}")
    public Rating findById(@PathVariable int id) {
        return ratingService.findById(id);
    }

    @GetMapping
    public List<Rating> findAll() {
        return ratingService.findAll();
    }
}
