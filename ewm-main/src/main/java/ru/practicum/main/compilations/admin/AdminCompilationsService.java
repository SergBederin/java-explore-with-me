package ru.practicum.main.compilations.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.NewCompilationDto;
import ru.practicum.main.compilations.dto.UpdateCompilationRequest;
import ru.practicum.main.compilations.model.Compilations;
import ru.practicum.main.compilations.repository.CompilationsRepository;
import ru.practicum.main.events.close.CloseEventsService;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.messages.ExceptionMessages;
import ru.practicum.main.messages.LogMessages;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.main.compilations.mapper.CompMap.mapToCompilations;
import static ru.practicum.main.compilations.mapper.CompMap.mapToCompilationsDto;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class AdminCompilationsService {
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
        log.debug(LogMessages.ADMIN_POST_COMPILATIONS.label);
        return mapToCompilationsDto(compilationsRepository.save(mapToCompilations(newCompilationDto, eventList)));
    }

    public void deleteCompilations(int compId) {
        compilationsRepository.findById(compId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_COMPILATIONS_EXCEPTION.label));
        log.debug(LogMessages.ADMIN_DELETE_COMPILATIONS_ID.label, compId);
        compilationsRepository.deleteById(compId);
    }

    public CompilationDto changeCompilations(int compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilations oldCompilations = compilationsRepository.findById(compId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_COMPILATIONS_EXCEPTION.label));

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

        log.debug(LogMessages.ADMIN_PATCH_COMPILATIONS_ID.label, compId);
        return mapToCompilationsDto(compilationsRepository.save(oldCompilations));
    }
}