package ru.practicum.compilations.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class CompilationsController {
    @Autowired
    private CompilationsService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/compilations")
    public CompilationDto createCompilations(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Выполняется запрос Post//compilations для добавления новой подборки.");
        return service.createCompilations(newCompilationDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/compilations/{compId}")
    public void deleteCompilations(@PathVariable(name = "compId") int compId) {
        log.info("Выполняется запрос Delete/compilations{compId} для удаления подборки по id: {}.", compId);
        service.deleteCompilations(compId);
    }

    @PatchMapping(path = "/compilations/{compId}")
    public CompilationDto changeCompilations(@PathVariable(name = "compId") int compId,
                                             @RequestBody @Valid UpdateCompilationDto updateCompilationDto) {
        log.info("Выполняется запрос Patch/compilations{compId} для обновления информации о подборке по id: {}.", compId);
        return service.changeCompilations(compId, updateCompilationDto);
    }
}
