package ru.practicum.main.compilations.open;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.messages.LogMessages;

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
        log.debug(LogMessages.TRY_PUBLIC_GET_COMPILATIONS.label);
        return service.getCompilations(pinned, from, size);
    }

    @GetMapping(path = "/compilations/{compId}")
    public CompilationDto getCompilationsById(@PathVariable(name = "compId") int compId) {
        log.debug(LogMessages.TRY_PUBLIC_GET_COMPILATIONS_ID.label, compId);
        return service.getCompilationsById(compId);
    }
}