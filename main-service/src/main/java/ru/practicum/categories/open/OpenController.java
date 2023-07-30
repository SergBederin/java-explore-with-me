package ru.practicum.categories.open;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.categories.dto.CategoryDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OpenController {
    @Autowired
    private OpenCategoriesService service;

    @GetMapping(path = "/categories")
    public List<CategoryDto> getCategories(@RequestParam(name = "from", defaultValue = "0") int from,
                                           @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Выполняется запрос Get/categories получения категории");
        return service.getCategories(from, size);
    }

    @GetMapping(path = "/categories/{catId}")
    public CategoryDto getCategoriesById(@PathVariable(name = "catId") int catId) {
        log.info("Выполняется запрос Get/categories/{catId} получения категории id = {}", catId);
        return service.getCategoriesById(catId);
    }
}