package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.ResponseDto;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<String> postHit(@Valid @RequestBody RequestDto requestDto) {
        log.info("Выполняется запрос Post/hit для добавления эндпоинта {}", requestDto);
        statsService.saveHit(requestDto);
        return new ResponseEntity<>("Информация сохранена", HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public List<ResponseDto> getStats(@RequestParam(name = "start") String start,
                                      @RequestParam(name = "end") String end,
                                      @RequestParam(name = "uris", required = false) List<String> uris,
                                      @RequestParam(name = "unique", defaultValue = "false") boolean unique) {
        log.info("Выполняется запрос GET/stats для получения статистики для эндпоинтов {}", uris);
        return statsService.getStats(start, end, uris, unique);
    }
}
