package com.pehlione.kafka.service;

import com.pehlione.kafka.dto.ProductForm;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Product> findAllByCategoryId(Long categoryId) {
        return productRepository.findAllByCategoryIdOrderByNameAsc(categoryId);
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Urun bulunamadi: " + id));
    }

    @Transactional
    public Product create(ProductForm form) {
        Category category = categoryService.findById(form.getCategoryId());
        Product product = Product.builder()
                .name(form.getName().trim())
                .description(normalize(form.getDescription()))
                .price(form.getPrice())
                .category(category)
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductForm form) {
        Product product = findById(id);
        Category category = categoryService.findById(form.getCategoryId());
        product.setName(form.getName().trim());
        product.setDescription(normalize(form.getDescription()));
        product.setPrice(form.getPrice());
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(findById(id));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
