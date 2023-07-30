package ru.practicum.categories.open;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoriesRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

import static ru.practicum.categories.mapper.CatMap.mapToCategoryDto;
import static ru.practicum.categories.mapper.CatMap.mapToListCategoryDto;
import static ru.practicum.utils.Page.paged;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class OpenCategoriesService {
    @Autowired
    private CategoriesRepository repository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable page = paged(from, size);
        log.info("Получение категории.");
        return mapToListCategoryDto(repository.findAll(page));
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoriesById(int catId) {
        log.debug("Получение информации о категории id = {}.", catId);
        return mapToCategoryDto(repository.findById(catId).orElseThrow(() -> new NotFoundException("Категория не найдена.")));
    }

    @Transactional(readOnly = true)
    public Category getCatById(int catId) {
        return repository.findById(catId).orElseThrow(() -> new NotFoundException("Подборка не найдена или недоступна."));
    }
}
