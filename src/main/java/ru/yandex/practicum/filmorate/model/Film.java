package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Valid
@Data
public class Film {
    private Long id;
    @NotBlank
    private String name;
    @Size(min = 1, max = 200)
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @Min(1)
    private int duration;
    private Set<Long> likes;

    public Film(Long id, String name, String description, LocalDate releaseDate, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = new HashSet<>();
    }

    public void validate() throws ValidationException {
        if (getName() == null || getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания фильма не может быть более 200 символов");
        }

        if (getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза фильма не может быть ранее 28 декабря 1895 года");
        }

        if (getDuration() < 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

    }
}
