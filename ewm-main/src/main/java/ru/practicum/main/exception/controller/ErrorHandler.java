package ru.practicum.main.exception.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.ValidTimeException;
import ru.practicum.main.exception.model.ApiError;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@RestControllerAdvice
public class ErrorHandler {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Объект не найден.")
                .status(HttpStatus.NOT_FOUND.toString())
                .timestamp(LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidTimeException(final ValidTimeException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Данные не удовлетворяет правилам создания.")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Нарушение целостности данных.")
                .status(HttpStatus.CONFLICT.toString())
                .timestamp(LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final SQLException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Нарушение целостности данных.")
                .status(HttpStatus.CONFLICT.toString())
                .timestamp(LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConflictException(final MethodArgumentNotValidException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Нарушение целостности данных.")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter))
                .build();
    }
}

