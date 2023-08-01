package ru.practicum.main.compilations.mapper;

import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.NewCompilationDto;
import ru.practicum.main.compilations.model.Compilations;
import ru.practicum.main.events.model.Event;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.main.events.mapper.EventsMap.mapToListEventShortDto;

public class CompMap {
    public static Compilations mapToCompilations(NewCompilationDto newCompilationDto, List<Event> eventList) {
        return Compilations.builder()
                .eventsWithCompilations(eventList)
                .pinned(newCompilationDto.isPinned())
                .title(newCompilationDto.getTitle())
                .build();
    }

    public static CompilationDto mapToCompilationsDto(Compilations compilations) {
        return CompilationDto.builder()
                .id(compilations.getId())
                .events(mapToListEventShortDto(compilations.getEventsWithCompilations()))
                .pinned(compilations.isPinned())
                .title(compilations.getTitle())
                .build();
    }

    public static List<CompilationDto> mapToListCompilationsDto(List<Compilations> listCompilations) {
        List<CompilationDto> listCompilationDto = new ArrayList<>();
        for (Compilations comp : listCompilations) {
            listCompilationDto.add(mapToCompilationsDto(comp));
        }

        return listCompilationDto;
    }
}