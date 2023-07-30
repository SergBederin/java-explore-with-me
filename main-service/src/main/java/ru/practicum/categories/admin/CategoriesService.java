package ru.practicum.categories.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoriesRepository;
import ru.practicum.exception.NotFoundException;

import static ru.practicum.categories.mapper.CatMap.mapToCategory;
import static ru.practicum.categories.mapper.CatMap.mapToCategoryDto;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class CategoriesService {
    @Autowired
    private CategoriesRepository repository;

    public CategoryDto createCategories(NewCategoryDto newCategoryDto) {
        log.info("Выполнено получение категории {}", newCategoryDto);
        return mapToCategoryDto(repository.save(mapToCategory(newCategoryDto)));
    }

    public void deleteCategories(int catId) {
        repository.findById(catId).orElseThrow(() -> new NotFoundException("Категория не найдена."));
        repository.deleteById(catId);
    }

    public CategoryDto changeCategories(int catId, CategoryDto categoryDto) {
        Category oldCategory = repository.findById(catId).orElseThrow();
        if (categoryDto.getName() != null) {
            oldCategory.setName(categoryDto.getName());
        }
        log.info("Категория изменена {}", oldCategory);
        return mapToCategoryDto(repository.save(oldCategory));
    }

    @Transactional(readOnly = true)
    public Category findCategoriesById(int catId) {
        return repository.findById(catId).orElseThrow(() -> new NotFoundException("Категория не найдена."));
    }
}