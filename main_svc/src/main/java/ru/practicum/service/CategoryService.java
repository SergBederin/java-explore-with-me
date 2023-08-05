package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.dto.categoty.CategoryMapper;
import ru.practicum.dto.categoty.NewCategoryDto;
import ru.practicum.exception.AlreadyExistException;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryJpaRepository categoryJpaRepository;

    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        /*проверка параметров*/
        String name = newCategoryDto.getName();
        if (categoryJpaRepository.findByName(name) != null) {
            throw new AlreadyExistException("Категория с именем " + name + " уже существует");
        }

        Category category = categoryJpaRepository.save(CategoryMapper.toCategory(newCategoryDto));
        return CategoryMapper.toDto(category);
    }

    public void deleteById(int catId) {
        categoryJpaRepository.findById(catId)
                .orElseThrow(() -> new ElementNotFoundException("Категория с id= " + catId + " не найдена"));
        try {
            categoryJpaRepository.deleteById(catId);
        } catch (RuntimeException ex) {
            throw new DataConflictException("Не удалось удалить категорию. Возможно, существуют связанные события");
        }
    }

    @Transactional
    public CategoryDto updateCategory(int catId, CategoryDto categoryDto) {
        String name = categoryDto.getName();

        Category category = categoryJpaRepository.findById(catId)
                .orElseThrow(() -> new ElementNotFoundException("категории с id=" + catId + " не существует"));
        if (category.getName().equals(name)) {
            return CategoryMapper.toDto(category);
        }
        if (categoryJpaRepository.findByName(name) != null) {
            throw new AlreadyExistException("категория с таким именем уже существует");
        }
        category.setName(categoryDto.getName());
        categoryJpaRepository.save(category);
        return CategoryMapper.toDto(category);
    }

    public List<CategoryDto> getAllCategories(int from, int size) {
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Category> categories = categoryJpaRepository.findAll(page).getContent();

        return categories.stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(int categoryId) {

        if (categoryId <= 0) {
            throw new BadParameterException("Id не может быть меньше 1");
        }

        Category category = categoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> new ElementNotFoundException("Элемент с id=" + categoryId + " не найден"));

        return CategoryMapper.toDto(category);
    }

}