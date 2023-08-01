package ru.practicum.main.compilations.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.NewCompilationDto;
import ru.practicum.main.compilations.dto.UpdateCompilationRequest;
import ru.practicum.main.messages.LogMessages;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationsController {
    @Autowired
    private AdminCompilationsService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/compilations")
    public CompilationDto createCompilations(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.debug(LogMessages.TRY_ADMIN_POST_COMPILATIONS.label);
        return service.createCompilations(newCompilationDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/compilations/{compId}")
    public void deleteCompilations(@PathVariable(name = "compId") int compId) {
        log.debug(LogMessages.TRY_ADMIN_DELETE_COMPILATIONS_ID.label, compId);
        service.deleteCompilations(compId);
    }

    @PatchMapping(path = "/compilations/{compId}")
    public CompilationDto changeCompilations(@PathVariable(name = "compId") int compId,
                                             @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        log.debug(LogMessages.TRY_ADMIN_PATCH_COMPILATIONS_ID.label, compId);
        return service.changeCompilations(compId, updateCompilationRequest);
    }
}