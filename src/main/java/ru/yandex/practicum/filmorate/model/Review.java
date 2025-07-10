package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Review {

    private Integer reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Integer useful;
}
