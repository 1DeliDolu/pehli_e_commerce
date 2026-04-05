package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.CategoryForm;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCategoryControllerTest {

    private CategoryService categoryService;
    private CommerceEventPublisher commerceEventPublisher;
    private AdminCategoryController adminCategoryController;

    @BeforeEach
    void setUp() {
        categoryService = mock(CategoryService.class);
        commerceEventPublisher = mock(CommerceEventPublisher.class);
        adminCategoryController = new AdminCategoryController(categoryService, commerceEventPublisher);
    }

    @Test
    void listShouldPopulateCategories() {
        Model model = new ExtendedModelMap();
        List<Category> categories = List.of(
                sampleCategory(1L, "Electronics", "Devices"),
                sampleCategory(2L, "Books", "Reading")
        );

        when(categoryService.findAll()).thenReturn(categories);

        String viewName = adminCategoryController.list(model);

        assertThat(viewName).isEqualTo("admin/categories/list");
        assertThat(model.getAttribute("categories")).isEqualTo(categories);
    }

    @Test
    void createFormShouldPrepareEmptyForm() {
        Model model = new ExtendedModelMap();

        String viewName = adminCategoryController.createForm(model);

        assertThat(viewName).isEqualTo("admin/categories/form");
        assertThat(model.getAttribute("categoryForm")).isInstanceOf(CategoryForm.class);
        assertThat(model.getAttribute("formAction")).isEqualTo("/admin/categories");
        assertThat(model.getAttribute("pageTitle")).isEqualTo("Yeni Kategori");
        assertThat(model.getAttribute("submitLabel")).isEqualTo("Kategoriyi Kaydet");
    }

    @Test
    void createShouldRedirectWhenCategoryIsCreated() {
        CategoryForm form = sampleCategoryForm("Electronics", "Devices");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "categoryForm");
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");
        Category category = sampleCategory(10L, "Electronics", "Devices");

        when(categoryService.create(form)).thenReturn(category);

        String viewName = adminCategoryController.create(
                form,
                bindingResult,
                model,
                authentication,
                redirectAttributes
        );

        assertThat(viewName).isEqualTo("redirect:/admin/categories");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("Kategori olusturuldu.");
        verify(commerceEventPublisher)
                .publishAdminCategoryEvent("category.created", "admin", category);
    }

    @Test
    void createShouldReturnFormWhenCategoryNameIsDuplicate() {
        CategoryForm form = sampleCategoryForm("Electronics", "Devices");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "categoryForm");
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");

        when(categoryService.create(form)).thenThrow(new IllegalArgumentException("duplicate name"));

        String viewName = adminCategoryController.create(
                form,
                bindingResult,
                model,
                authentication,
                redirectAttributes
        );

        assertThat(viewName).isEqualTo("admin/categories/form");
        assertThat(bindingResult.hasFieldErrors("name")).isTrue();
        assertThat(bindingResult.getFieldError("name").getDefaultMessage()).isEqualTo("duplicate name");
        assertThat(model.getAttribute("formAction")).isEqualTo("/admin/categories");
        assertThat(model.getAttribute("pageTitle")).isEqualTo("Yeni Kategori");
        assertThat(model.getAttribute("submitLabel")).isEqualTo("Kategoriyi Kaydet");
        verify(commerceEventPublisher, never())
                .publishAdminCategoryEvent(any(), any(), any());
    }

    @Test
    void editFormShouldPopulateExistingCategory() {
        Model model = new ExtendedModelMap();
        Category category = sampleCategory(20L, "Books", "Reading");

        when(categoryService.findById(20L)).thenReturn(category);

        String viewName = adminCategoryController.editForm(20L, model);

        assertThat(viewName).isEqualTo("admin/categories/form");
        assertThat(model.getAttribute("formAction")).isEqualTo("/admin/categories/20");
        assertThat(model.getAttribute("pageTitle")).isEqualTo("Kategori Duzenle");
        assertThat(model.getAttribute("submitLabel")).isEqualTo("Degisiklikleri Kaydet");

        CategoryForm categoryForm = (CategoryForm) model.getAttribute("categoryForm");
        assertThat(categoryForm.getName()).isEqualTo("Books");
        assertThat(categoryForm.getDescription()).isEqualTo("Reading");
    }

    @Test
    void updateShouldRedirectWhenCategoryIsUpdated() {
        CategoryForm form = sampleCategoryForm("Office", "Supplies");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "categoryForm");
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");
        Category category = sampleCategory(30L, "Office", "Supplies");

        when(categoryService.update(30L, form)).thenReturn(category);

        String viewName = adminCategoryController.update(
                30L,
                form,
                bindingResult,
                model,
                authentication,
                redirectAttributes
        );

        assertThat(viewName).isEqualTo("redirect:/admin/categories");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("Kategori guncellendi.");
        verify(commerceEventPublisher)
                .publishAdminCategoryEvent("category.updated", "admin", category);
    }

    @Test
    void deleteShouldRedirectWithSuccessMessage() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");
        Category category = sampleCategory(40L, "Garden", "Outdoor");

        when(categoryService.findById(40L)).thenReturn(category);

        String viewName = adminCategoryController.delete(40L, authentication, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/admin/categories");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("Kategori silindi.");
        verify(categoryService).delete(40L);
        verify(commerceEventPublisher)
                .publishAdminCategoryEvent("category.deleted", "admin", category);
    }

    @Test
    void deleteShouldRedirectWithErrorMessageWhenCategoryCannotBeDeleted() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pw", "ROLE_ADMIN");
        Category category = sampleCategory(50L, "Garden", "Outdoor");

        when(categoryService.findById(50L)).thenReturn(category);
        doThrow(new IllegalStateException("category has products")).when(categoryService).delete(50L);

        String viewName = adminCategoryController.delete(50L, authentication, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/admin/categories");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("errorMessage")
                .isEqualTo("category has products");
        verify(commerceEventPublisher, never())
                .publishAdminCategoryEvent(eq("category.deleted"), eq("admin"), any());
    }

    private CategoryForm sampleCategoryForm(String name, String description) {
        CategoryForm form = new CategoryForm();
        form.setName(name);
        form.setDescription(description);
        return form;
    }

    private Category sampleCategory(Long id, String name, String description) {
        return Category.builder()
                .id(id)
                .name(name)
                .description(description)
                .build();
    }
}
