package ru.practicum.compilations.open;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.compilations.dto.CompilationDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OpenCompilationsController {
    @Autowired
    private OpenCompilationsService service;

    @GetMapping(path = "/compilations")
    public List<CompilationDto> getCompilations(@RequestParam(name = "pinned", required = false) String pinned,
                                                @RequestParam(name = "from", defaultValue = "0") int from,
                                                @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Выполняется запрос Get/compilations для получения подборок событий.");
        return service.getCompilations(pinned, from, size);
    }

    @GetMapping(path = "/compilations/{compId}")
    public CompilationDto getCompilationsById(@PathVariable(name = "compId") int compId) {
        log.info("Выполняется запрос Get/compilations для получения подборок событий по id: {}.", compId);
        return service.getCompilationsById(compId);
    }
}
