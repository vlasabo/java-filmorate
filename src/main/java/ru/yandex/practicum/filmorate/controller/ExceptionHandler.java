package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final MethodArgumentNotValidException e) {
        return new ErrorResponse(
                "Data validation error", e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(final RuntimeException e) {
        return new ErrorResponse(
                "No data found", e.getMessage()
        );
    }
}
