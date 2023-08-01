package ru.practicum.main.categories.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.NewCategoryDto;
import ru.practicum.main.messages.LogMessages;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoriesController {
    @Autowired
    private AdminCategoriesService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/categories")
    public CategoryDto createCategories(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.debug(LogMessages.TRY_ADMIN_POST_CATEGORIES.label);
        return service.createCategories(newCategoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/categories/{catId}")
    public void deleteCategories(@PathVariable(name = "catId") int catId) {
        log.debug(LogMessages.TRY_ADMIN_DELETE_CATEGORIES_ID.label, catId);
        service.deleteCategories(catId);
    }

    @PatchMapping(path = "/categories/{catId}")
    public CategoryDto changeCategories(@PathVariable(name = "catId") int catId,
                                        @RequestBody @Valid CategoryDto categoryDto) {
        log.debug(LogMessages.TRY_ADMIN_PATCH_CATEGORIES_ID.label, catId);
        return service.changeCategories(catId, categoryDto);
    }
}