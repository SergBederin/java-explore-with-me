package ru.practicum.categories.mapper;

import org.springframework.data.domain.Page;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CatMap {
    public static Category mapToCategory(CategoryDto categoryDto) {
        return Category.builder()
                .name(categoryDto.getName())
                .build();
    }

    public static Category mapToCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public static CategoryDto mapToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static List<CategoryDto> mapToListCategoryDto(Page<Category> listCategory) {
        List<CategoryDto> listCategoryDto = new ArrayList<>();
        for (Category cat : listCategory) {
            listCategoryDto.add(mapToCategoryDto(cat));
        }
        return listCategoryDto;
    }
}
