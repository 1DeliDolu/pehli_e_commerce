package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.ProductForm;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.service.CategoryService;
import com.pehlione.kafka.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminProductControllerTest {

    private ProductService productService;
    private CategoryService categoryService;
    private CommerceEventPublisher commerceEventPublisher;
    private AdminProductController adminProductController;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        categoryService = mock(CategoryService.class);
        commerceEventPublisher = mock(CommerceEventPublisher.class);
        adminProductController = new AdminProductController(productService, categoryService, commerceEventPublisher);
    }

    @Test
    void listShouldPopulateProducts() {
        Model model = new ExtendedModelMap();
        List<Product> products = List.of(sampleProduct(1L, "Keyboard", sampleCategory(10L, "Electronics")));

        when(productService.findAll()).thenReturn(products);

        String viewName = adminProductController.list(model);

        assertThat(viewName).isEqualTo("admin/products/list");
        assertThat(model.getAttribute("products")).isEqualTo(products);
    }

    @Test
    void createFormShouldPopulateCategories() {
        Model model = new ExtendedModelMap();
        List<Category> categories = List.of(sampleCategory(10L, "Electronics"));

        when(categoryService.findAll()).thenReturn(categories);

        String viewName = adminProductController.createForm(model);

        assertThat(viewName).isEqualTo("admin/products/form");
        assertThat(model.getAttribute("productForm")).isInstanceOf(ProductForm.class);
        assertThat(model.getAttribute("categories")).isEqualTo(categories);
        assertThat(model.getAttribute("formAction")).isEqualTo("/admin/products");
    }

    @Test
    void createShouldRedirectWhenProductIsCreated() {
        ProductForm form = sampleProductForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "productForm");
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");
        Product product = sampleProduct(1L, "Keyboard", sampleCategory(10L, "Electronics"));

        when(productService.create(form)).thenReturn(product);

        String viewName = adminProductController.create(form, bindingResult, model, authentication, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/admin/products");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("Urun olusturuldu.");
        verify(commerceEventPublisher).publishAdminProductEvent("product.created", "admin", product);
    }

    @Test
    void createShouldReturnFormWhenServiceThrowsValidationError() {
        ProductForm form = sampleProductForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "productForm");
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");
        List<Category> categories = List.of(sampleCategory(10L, "Electronics"));

        when(productService.create(form)).thenThrow(new IllegalArgumentException("duplicate product"));
        when(categoryService.findAll()).thenReturn(categories);

        String viewName = adminProductController.create(form, bindingResult, model, authentication, redirectAttributes);

        assertThat(viewName).isEqualTo("admin/products/form");
        assertThat(model.getAttribute("formErrorMessage")).isEqualTo("duplicate product");
        assertThat(model.getAttribute("categories")).isEqualTo(categories);
        verify(commerceEventPublisher, never()).publishAdminProductEvent(any(), any(), any());
    }

    @Test
    void editFormShouldPopulateExistingProduct() {
        Model model = new ExtendedModelMap();
        Category category = sampleCategory(10L, "Electronics");
        Product product = sampleProduct(1L, "Keyboard", category);
        List<Category> categories = List.of(category);

        when(productService.findById(1L)).thenReturn(product);
        when(categoryService.findAll()).thenReturn(categories);

        String viewName = adminProductController.editForm(1L, model);

        assertThat(viewName).isEqualTo("admin/products/form");
        assertThat(model.getAttribute("categories")).isEqualTo(categories);
        assertThat(model.getAttribute("formAction")).isEqualTo("/admin/products/1");
        ProductForm form = (ProductForm) model.getAttribute("productForm");
        assertThat(form.getName()).isEqualTo("Keyboard");
        assertThat(form.getCategoryId()).isEqualTo(10L);
    }

    @Test
    void updateShouldRedirectWhenProductIsUpdated() {
        ProductForm form = sampleProductForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "productForm");
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");
        Product product = sampleProduct(1L, "Keyboard", sampleCategory(10L, "Electronics"));

        when(productService.update(1L, form)).thenReturn(product);

        String viewName = adminProductController.update(1L, form, bindingResult, model, authentication, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/admin/products");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("Urun guncellendi.");
        verify(commerceEventPublisher).publishAdminProductEvent("product.updated", "admin", product);
    }

    @Test
    void deleteShouldRedirectWithSuccessMessage() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");
        Product product = sampleProduct(1L, "Keyboard", sampleCategory(10L, "Electronics"));

        when(productService.findById(1L)).thenReturn(product);

        String viewName = adminProductController.delete(1L, authentication, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/admin/products");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("Urun silindi.");
        verify(productService).delete(1L);
        verify(commerceEventPublisher).publishAdminProductEvent("product.deleted", "admin", product);
    }

    @Test
    void deleteShouldRedirectWithErrorMessageWhenProductCannotBeDeleted() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");
        Product product = sampleProduct(1L, "Keyboard", sampleCategory(10L, "Electronics"));

        when(productService.findById(1L)).thenReturn(product);
        doThrow(new IllegalArgumentException("cannot delete")).when(productService).delete(1L);

        String viewName = adminProductController.delete(1L, authentication, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/admin/products");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("errorMessage")
                .isEqualTo("cannot delete");
        verify(commerceEventPublisher, never()).publishAdminProductEvent(eq("product.deleted"), eq("admin"), any());
    }

    private ProductForm sampleProductForm() {
        ProductForm form = new ProductForm();
        form.setName("Keyboard");
        form.setDescription("Mechanical");
        form.setPrice(BigDecimal.valueOf(129.90));
        form.setCategoryId(10L);
        return form;
    }

    private Product sampleProduct(Long id, String name, Category category) {
        return Product.builder()
                .id(id)
                .name(name)
                .description("Mechanical")
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
