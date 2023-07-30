package ru.practicum.compilations.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationDto;
import ru.practicum.compilations.model.Compilations;
import ru.practicum.compilations.repository.CompilationsRepository;
import ru.practicum.events.close.CloseEventsService;
import ru.practicum.events.model.Event;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.compilations.mapper.CompMap.mapToCompilations;
import static ru.practicum.compilations.mapper.CompMap.mapToCompilationsDto;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class CompilationsService {
    @Autowired
    private CompilationsRepository compilationsRepository;
    @Autowired
    private CloseEventsService eventsService;

    public CompilationDto createCompilations(NewCompilationDto newCompilationDto) {
        List<Event> eventList = new ArrayList<>();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            for (Integer eventId : newCompilationDto.getEvents()) {
                eventList.add(eventsService.getEventById(eventId));
            }
        }
        log.info("Выполнено добавление новой подборки.");
        return mapToCompilationsDto(compilationsRepository.save(mapToCompilations(newCompilationDto, eventList)));
    }

    public void deleteCompilations(int compId) {
        compilationsRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка не найдена."));
        log.info("Выполнено  удаление подборки по id = {}.", compId);
        compilationsRepository.deleteById(compId);
    }

    public CompilationDto changeCompilations(int compId, UpdateCompilationDto updateCompilationRequest) {
        Compilations oldCompilations = compilationsRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка не найдена"));

        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            List<Event> eventList = new ArrayList<>();

            for (Integer eventId : updateCompilationRequest.getEvents()) {
                eventList.add(eventsService.getEventById(eventId));
            }

            oldCompilations.setEventsWithCompilations(eventList);
        }
        if (updateCompilationRequest.getPinned() != null) {
            oldCompilations.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null && !updateCompilationRequest.getTitle().isEmpty()) {
            oldCompilations.setTitle(updateCompilationRequest.getTitle());
        }
        log.info("Выполнено обновление информации о подборке по id= {}.", compId);
        return mapToCompilationsDto(compilationsRepository.save(oldCompilations));
    }
}
