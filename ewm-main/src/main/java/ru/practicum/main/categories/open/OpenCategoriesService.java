package ru.practicum.main.categories.open;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.model.Category;
import ru.practicum.main.categories.repository.CategoriesRepository;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.messages.ExceptionMessages;
import ru.practicum.main.messages.LogMessages;

import java.util.List;

import static ru.practicum.main.categories.mapper.CatMap.mapToCategoryDto;
import static ru.practicum.main.categories.mapper.CatMap.mapToListCategoryDto;
import static ru.practicum.main.utils.Page.paged;

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
        log.debug(LogMessages.PUBLIC_GET_CATEGORIES.label);
        return mapToListCategoryDto(repository.findAll(page));
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoriesById(int catId) {
        log.debug(LogMessages.PUBLIC_GET_CATEGORIES_ID.label, catId);
        return mapToCategoryDto(repository.findById(catId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_CATEGORIES_EXCEPTION.label)));
    }

    @Transactional(readOnly = true)
    public Category getCatById(int catId) {
        return repository.findById(catId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_COMPILATIONS_EXCEPTION.label));
    }
}