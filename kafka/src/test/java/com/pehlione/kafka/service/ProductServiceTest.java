package com.pehlione.kafka.service;

import com.pehlione.kafka.dto.ProductForm;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private ProductRepository productRepository;
    private CategoryService categoryService;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryService = mock(CategoryService.class);
        productService = new ProductService(productRepository, categoryService);
    }

    @Test
    void findAllShouldReturnRepositoryResult() {
        List<Product> products = List.of(sampleProduct(1L, "Keyboard", sampleCategory(10L, "Electronics")));

        when(productRepository.findAllByOrderByNameAsc()).thenReturn(products);

        assertThat(productService.findAll()).isEqualTo(products);
    }

    @Test
    void createShouldTrimNameAndNormalizeBlankDescription() {
        ProductForm form = new ProductForm();
        form.setName("  Keyboard  ");
        form.setDescription("   ");
        form.setPrice(BigDecimal.valueOf(129.90));
        form.setCategoryId(10L);
        Category category = sampleCategory(10L, "Electronics");

        when(categoryService.findById(10L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create(form);

        assertThat(result.getName()).isEqualTo("Keyboard");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getCategory()).isEqualTo(category);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateShouldReplaceProductFields() {
        ProductForm form = new ProductForm();
        form.setName("  Monitor  ");
        form.setDescription("  IPS panel  ");
        form.setPrice(BigDecimal.valueOf(289.00));
        form.setCategoryId(20L);
        Product existing = sampleProduct(1L, "Keyboard", sampleCategory(10L, "Electronics"));
        Category newCategory = sampleCategory(20L, "Displays");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryService.findById(20L)).thenReturn(newCategory);
        when(productRepository.save(existing)).thenReturn(existing);

        Product result = productService.update(1L, form);

        assertThat(result.getName()).isEqualTo("Monitor");
        assertThat(result.getDescription()).isEqualTo("IPS panel");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(289.00));
        assertThat(result.getCategory()).isEqualTo(newCategory);
    }

    @Test
    void findByIdShouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Urun bulunamadi: 99");
    }

    private Product sampleProduct(Long id, String name, Category category) {
        return Product.builder()
                .id(id)
                .name(name)
                .description(name + " desc")
                .price(BigDecimal.valueOf(129.90))
                .category(category)
                .build();
    }

    private Category sampleCategory(Long id, String name) {
        return Category.builder()
                .id(id)
                .name(name)
                .description(name + " desc")
                .build();
    }
}
