package ru.practicum.controller.publ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@Slf4j
public class CompilationControllerPublic {

    private final CompilationService compilationService;

    @Autowired
    public CompilationControllerPublic(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(name = "pinned", defaultValue = "false") boolean pinned,
                                                @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                                @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        List<CompilationDto> compnDtoList = compilationService.getAllComps(pinned, from, size);
        log.info("Выполняется запрос Get/compilations для получения списка всех подборок через Публичный контроллер");
        return compnDtoList;
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilations(@PathVariable int compId) {
        CompilationDto compilationDto = compilationService.getCompById(compId);
        log.info("Выполняется запрос Get/compilations/{compId} для получения подборки с id={} через Публичный контроллер", compId);
        return compilationDto;
    }

}
