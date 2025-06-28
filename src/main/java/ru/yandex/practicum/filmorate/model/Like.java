package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Like {
    int id;
    int user_id;
    int film_id;
}
