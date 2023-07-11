package ru.practicum.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.ResponseDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Stats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
@Slf4j
public class StatsService {
    @Autowired
    private StatsRepository repository;

    public void saveHit(RequestDto requestDto) {
        Stats state = StatsMapper.mapToStat(requestDto);
        state.setTimestamp(LocalDateTime.now());
        log.info("Добавлена запись статистики = {}", requestDto);
        repository.save(state);
    }

    public List<ResponseDto> getStats(String start, String end, List<String> uri, boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime timeStart = LocalDateTime.parse(start, formatter);
        LocalDateTime timeEnd = LocalDateTime.parse(end, formatter);
        List<ResponseDto> listResponseStat = new ArrayList<>();

        if (timeStart.isAfter(timeEnd)) {
            throw new ValidationException("Неправильно указано время для поиска!");
        }

        if (uri != null && !uri.isEmpty()) {
            if (unique) {
                listResponseStat.addAll(repository.findStatUriUnique(timeStart, timeEnd, uri));
            } else {
                listResponseStat.addAll(repository.findStatUri(timeStart, timeEnd, uri));
            }
        } else {
            if (unique) {
                listResponseStat.addAll(repository.findStatUnique(timeStart, timeEnd));
            } else {
                listResponseStat.addAll(repository.findStat(timeStart, timeEnd));
            }
        }

        if (listResponseStat.isEmpty()) {
            throw new NotFoundException("Записи статистики не найдены.");
        } else {
            log.info("Получены записи статистики.");
            return listResponseStat.stream().sorted(Comparator.comparing(ResponseDto::getHits).reversed()).collect(Collectors.toList());
        }
    }

}
