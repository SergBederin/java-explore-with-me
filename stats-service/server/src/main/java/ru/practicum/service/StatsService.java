package ru.practicum.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.ResponseDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Stats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@NoArgsConstructor
@Slf4j
public class StatsService {
    @Autowired
    private StatsRepository repository;

    public void hit(RequestDto requestDto) {
        Stats state = StatsMapper.mapToStat(requestDto);
        state.setTimestamp(LocalDateTime.now());
        log.info("Добавлена запись статистики = {}", requestDto);
        repository.save(state);
    }

    public List<ResponseDto> stats(String start, String end, List<String> uris, boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime timeStart = LocalDateTime.parse(start, formatter);
        LocalDateTime timeEnd = LocalDateTime.parse(end, formatter);
        if (timeStart.isAfter(timeEnd)) {
            throw new ValidationException("Неправильно указано время для поиска!");
        }

        if (uris != null && !uris.isEmpty()) {
            if (unique) {
                return repository.findStatUriUnique(timeStart, timeEnd, uris);
            } else {
                return repository.findStatUri(timeStart, timeEnd, uris);
            }
        } else {
            if (unique) {
                return repository.findStatUnique(timeStart, timeEnd);
            } else {
                return repository.findStat(timeStart, timeEnd);
            }
        }
    }
}

