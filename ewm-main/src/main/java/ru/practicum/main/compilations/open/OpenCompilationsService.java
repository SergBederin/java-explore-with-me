package ru.practicum.main.compilations.open;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.repository.CompilationsRepository;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.messages.ExceptionMessages;
import ru.practicum.main.messages.LogMessages;

import java.util.List;

import static ru.practicum.main.compilations.mapper.CompMap.mapToCompilationsDto;
import static ru.practicum.main.compilations.mapper.CompMap.mapToListCompilationsDto;
import static ru.practicum.main.utils.Page.paged;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class OpenCompilationsService {
    @Autowired
    private CompilationsRepository repository;

    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(String pinned, int from, int size) {
        Pageable page = paged(from, size);
        log.debug(LogMessages.PUBLIC_GET_COMPILATIONS.label);
        return mapToListCompilationsDto(repository.findCompilationsByPinnedIs(Boolean.parseBoolean(pinned), page));
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationsById(int compId) {
        log.debug(LogMessages.PUBLIC_GET_COMPILATIONS_ID.label, compId);
        return mapToCompilationsDto(repository.findById(compId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_COMPILATIONS_EXCEPTION.label)));
    }
}