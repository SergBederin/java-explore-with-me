package ru.practicum.categories.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class CategoriesController {
    @Autowired
    private CategoriesService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/categories")
    public CategoryDto createCategories(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Выполняется запрос Post/categories для категории.");
        return service.createCategories(newCategoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/categories/{catId}")
    public void deleteCategories(@PathVariable(name = "catId") int catId) {
        log.info("Выполняется запрос Delete/categories/{catId} для  удаления категории по id ={}", catId);
        service.deleteCategories(catId);
    }

    @PatchMapping(path = "/categories/{catId}")
    public CategoryDto changeCategories(@PathVariable(name = "catId") int catId,
                                        @RequestBody @Valid CategoryDto categoryDto) {
        log.info("Выполняется запрос Patch/categories/{catId} для  изменения категории по id ={}", catId);
        return service.changeCategories(catId, categoryDto);
    }
}
