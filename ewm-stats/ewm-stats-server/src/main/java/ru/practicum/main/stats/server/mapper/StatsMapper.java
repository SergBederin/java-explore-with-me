package ru.practicum.main.stats.server.mapper;

import ru.practicum.main.stats.dto.RequestDto;
import ru.practicum.main.stats.dto.ResponseDto;
import ru.practicum.main.stats.server.model.Stats;

public class StatsMapper {
    public static Stats mapToStat(RequestDto requestDto) {
        return Stats.builder()
                .app(requestDto.getApp())
                .uri(requestDto.getUri())
                .ip(requestDto.getIp())
                .build();
    }

    public static ResponseDto mapToResponseDto(Stats stat) {
        return ResponseDto.builder()
                .app(stat.getApp())
                .uri(stat.getUri())
                .build();
    }
}
