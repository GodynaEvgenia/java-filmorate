package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

import java.util.Collections;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(final ValidationException e) {
        log.error("Ошибка валидации {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации", Collections.singletonList(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final ResourceNotFoundException e) {
        log.error("Искомый объект не найдена {}", e.getMessage());
        return new ErrorResponse(
                "Искомый объект не найден",
                Collections.singletonList(e.getMessage())
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerError(final InternalServerException e) {
        log.error("Ошибка сервера {}", e.getMessage());
        return new ErrorResponse(
                "Ошибка сервера",
                Collections.singletonList(e.getMessage())
        );
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onConstraintValidationException(ConstraintViolationException e) {
        log.error("Параметры не прошли валидацию Spring  {}", e.getMessage());
        return new ErrorResponse(
                "Объект не прошёл валидацию Spring ",
                Collections.singletonList(e.getMessage())
        );
    }
}
