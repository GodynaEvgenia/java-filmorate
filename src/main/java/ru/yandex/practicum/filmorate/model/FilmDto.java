package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Data
@RequiredArgsConstructor
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Genre> genres;
    private UniObject mpa;
    private Set<Director> directors;
    private Set<Long> likes = new HashSet<>();

    public FilmDto(Long id, String name, String description, LocalDate releaseDate, int duration, UniObject mpa, Set<Long> likes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genres = new LinkedHashSet<>();
        this.mpa = mpa;
        this.likes = likes;
        this.directors = new LinkedHashSet<>();
    }
}
