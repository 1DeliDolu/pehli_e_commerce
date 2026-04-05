package com.pehlione.kafka.api;

import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.service.CategoryService;
import com.pehlione.kafka.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    private ProductService productService;
    private CategoryService categoryService;
    private CommerceEventPublisher commerceEventPublisher;
    private ProductController productController;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        categoryService = mock(CategoryService.class);
        commerceEventPublisher = mock(CommerceEventPublisher.class);
        productController = new ProductController(productService, categoryService, commerceEventPublisher);
    }

    @Test
    void productsShouldReturnAllProductsWhenNoCategoryFilterExists() {
        Model model = new ExtendedModelMap();
        HttpSession session = mock(HttpSession.class);
        List<Product> products = List.of(sampleProduct(1L, "Keyboard", sampleCategory(10L, "Electronics")));

        when(productService.findAll()).thenReturn(products);

        String viewName = productController.products(null, model, session);

        assertThat(viewName).isEqualTo("products/index");
        assertThat(model.getAttribute("products")).isEqualTo(products);
    }

    @Test
    void productsShouldPublishCategoryViewedWhenFilterExists() {
        Model model = new ExtendedModelMap();
        HttpSession session = mock(HttpSession.class);
        Category category = sampleCategory(10L, "Electronics");
        List<Product> products = List.of(sampleProduct(1L, "Keyboard", category));

        when(categoryService.findById(10L)).thenReturn(category);
        when(productService.findAllByCategoryId(10L)).thenReturn(products);

        String viewName = productController.products(10L, model, session);

        assertThat(viewName).isEqualTo("products/index");
        assertThat(model.getAttribute("selectedCategory")).isEqualTo(category);
        assertThat(model.getAttribute("products")).isEqualTo(products);
        verify(commerceEventPublisher).publishCategoryViewed(session, category);
    }

    @Test
    void productDetailsShouldPopulateImagesAndPublishViewEvent() {
        Model model = new ExtendedModelMap();
        HttpSession session = mock(HttpSession.class);
        Product product = sampleProduct(7L, "Monitor", sampleCategory(10L, "Electronics"));

        when(productService.findById(7L)).thenReturn(product);

        String viewName = productController.productDetails(7L, model, session);

        assertThat(viewName).isEqualTo("products/detail");
        assertThat(model.getAttribute("product")).isEqualTo(product);
        assertThat(model.getAttribute("productImages"))
                .isEqualTo(List.of(
                        "https://picsum.photos/seed/kafka-product-7-1/1200/1400",
                        "https://picsum.photos/seed/kafka-product-7-2/1200/1400",
                        "https://picsum.photos/seed/kafka-product-7-3/1200/1400",
                        "https://picsum.photos/seed/kafka-product-7-4/1200/1400"
                ));
        verify(commerceEventPublisher).publishProductViewed(session, product);
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
