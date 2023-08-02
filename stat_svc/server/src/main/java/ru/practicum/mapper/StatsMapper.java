package ru.practicum.mapper;

import ru.practicum.dto.RequestDto;
import ru.practicum.dto.ResponseDto;
import ru.practicum.model.Stats;

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
