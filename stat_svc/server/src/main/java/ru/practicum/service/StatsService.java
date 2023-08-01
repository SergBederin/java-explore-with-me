package ru.practicum.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.main.stats.dto.RequestDto;
import ru.practicum.main.stats.dto.RequestParamDto;
import ru.practicum.main.stats.dto.ResponseDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Stats;
import ru.practicum.repository.StatsRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@NoArgsConstructor
@Slf4j
public class StatsService {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private StatsRepository repository;

    public void hit(RequestDto requestDto) {
        Stats state = StatsMapper.mapToStat(requestDto);
        state.setTimestamp(LocalDateTime.now());
        log.info("Добавлена запись статистики = {}", requestDto);
        repository.save(state);
    }

    public List<ResponseDto> stats(String start, String end, List<String> uris, boolean unique) {
        RequestParamDto requestParamDto = new RequestParamDto(start, end, uris, unique); //собираем все параметры запроса в отдельный DTO
        String startDecoded = URLDecoder.decode(requestParamDto.getStart(), StandardCharsets.UTF_8);
        String endDecoded = URLDecoder.decode(requestParamDto.getEnd(), StandardCharsets.UTF_8);

        /*преобразование полученных строк в LocalDateTime*/
        LocalDateTime timeStart = LocalDateTime.parse(startDecoded, TIME_FORMAT);
        LocalDateTime timeEnd = LocalDateTime.parse(endDecoded, TIME_FORMAT);
        /*DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime timeStart = LocalDateTime.parse(start, formatter);
        LocalDateTime timeEnd = LocalDateTime.parse(end, formatter);*/
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

