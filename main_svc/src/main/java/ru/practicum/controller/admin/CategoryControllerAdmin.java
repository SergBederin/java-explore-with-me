package ru.practicum.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.dto.categoty.NewCategoryDto;
import ru.practicum.service.CategoryService;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/categories")
@Slf4j
public class CategoryControllerAdmin {

    private final CategoryService categoryService;

    @Autowired
    public CategoryControllerAdmin(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) //201
    public CategoryDto postCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        CategoryDto categoryDto = categoryService.create(newCategoryDto);
        log.info("Выполняется запрос Post/admin/categories для создания новой категории id={}, name={}", categoryDto.getId(), categoryDto.getName());
        return categoryDto;
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteUser(@PathVariable(name = "catId") int catId) {
        categoryService.deleteById(catId);
        log.info("Выполняется запрос Delete/admin/categories/{catId} удаления категории с Id={}", catId);
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK) //200
    public CategoryDto patchCategory(@PathVariable(name = "catId") int catId,
                                     @Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto updatedCategoryDto = categoryService.updateCategory(catId, categoryDto);
        log.info("Выполняется запрос Patchadmin/categories/{catId} на зменение категории с Id={}, name={}", updatedCategoryDto.getId(), updatedCategoryDto.getName());
        return updatedCategoryDto;
    }
}
