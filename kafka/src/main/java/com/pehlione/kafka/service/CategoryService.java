package com.pehlione.kafka.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pehlione.kafka.dto.CategoryForm;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.repository.CategoryRepository;
import com.pehlione.kafka.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("The CategoryService failed to find a category. id=" + id));
    }

    @Transactional
    public Category create(CategoryForm form) {
        validateUniqueName(form.getName(), null);
        Category category = Category.builder()
                .name(form.getName().trim())
                .description(normalize(form.getDescription()))
                .build();
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, CategoryForm form) {
        Category category = findById(id);
        validateUniqueName(form.getName(), id);
        category.setName(form.getName().trim());
        category.setDescription(normalize(form.getDescription()));
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("The CategoryService failed to delete a category. There are products associated with this category. id=" + id);
        }
        categoryRepository.delete(findById(id));
    }

    private void validateUniqueName(String name, Long id) {
        String normalized = name.trim();
        boolean exists = id == null
                ? categoryRepository.existsByNameIgnoreCase(normalized)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(normalized, id);
        if (exists) {
            throw new IllegalArgumentException("The CategoryService failed to create/update a category. The category name is already in use. name=" + name);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
