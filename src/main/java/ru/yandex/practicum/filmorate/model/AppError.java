package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter

@RequiredArgsConstructor
public class AppError {
    private int statusCode;
    private String message;
    private String error;

    /*public AppError() {
    }*/

    public AppError(int statusCode, String message, String error) {
        this.statusCode = statusCode;
        this.message = message;
        this.error = error;
    }
}
