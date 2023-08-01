package ru.practicum.main.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * DTO Для передачи параметров запроса в методы сервиса
 */
@Getter
@AllArgsConstructor
public class RequestParamDto {
    private final String start; //время начала выборки статистики
    private final String end; //время конца выборки статистики
    private final List<String> uris; //массив URI
    private final boolean unique; //флаг уникальности IP- источников запросов в процессе учета статистики
}