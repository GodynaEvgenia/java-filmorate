package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    public void validate() {
       if (getName() == null || getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }

        if (getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания не может быть более 200 символов");
        }

        if (getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть ранее 28 декабря 1895 года");
        }

        if (getDuration() < 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

    }
}
