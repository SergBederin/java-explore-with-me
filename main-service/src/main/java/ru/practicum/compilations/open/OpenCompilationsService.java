package ru.practicum.compilations.open;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.repository.CompilationsRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

import static ru.practicum.compilations.mapper.CompMap.mapToCompilationsDto;
import static ru.practicum.compilations.mapper.CompMap.mapToListCompilationsDto;
import static ru.practicum.utils.Page.paged;

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
        log.info("Выполнено получение подборок событий");
        return mapToListCompilationsDto(repository.findCompilationsByPinnedIs(Boolean.parseBoolean(pinned), page));
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationsById(int compId) {
        log.info("Выполнено получение подборок событий по id= {}.", compId);
        return mapToCompilationsDto(repository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка не найдена.")));
    }
}
