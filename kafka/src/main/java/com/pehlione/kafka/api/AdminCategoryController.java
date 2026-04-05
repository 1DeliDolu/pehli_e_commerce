package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.CategoryForm;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final CommerceEventPublisher commerceEventPublisher;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        fillFormModel(model, "/admin/categories", "Yeni Kategori", "Kategoriyi Kaydet");
        return "admin/categories/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult bindingResult,
                         Model model,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            fillFormModel(model, "/admin/categories", "Yeni Kategori", "Kategoriyi Kaydet");
            return "admin/categories/form";
        }

        try {
            Category category = categoryService.create(form);
            commerceEventPublisher.publishAdminCategoryEvent("category.created", authentication.getName(), category);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori olusturuldu.");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            fillFormModel(model, "/admin/categories", "Yeni Kategori", "Kategoriyi Kaydet");
            return "admin/categories/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        CategoryForm form = new CategoryForm();
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        model.addAttribute("categoryForm", form);
        fillFormModel(model, "/admin/categories/" + id, "Kategori Duzenle", "Degisiklikleri Kaydet");
        return "admin/categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult bindingResult,
                         Model model,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            fillFormModel(model, "/admin/categories/" + id, "Kategori Duzenle", "Degisiklikleri Kaydet");
            return "admin/categories/form";
        }

        try {
            Category category = categoryService.update(id, form);
            commerceEventPublisher.publishAdminCategoryEvent("category.updated", authentication.getName(), category);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori guncellendi.");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            fillFormModel(model, "/admin/categories/" + id, "Kategori Duzenle", "Degisiklikleri Kaydet");
            return "admin/categories/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findById(id);
            categoryService.delete(id);
            commerceEventPublisher.publishAdminCategoryEvent("category.deleted", authentication.getName(), category);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori silindi.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    private void fillFormModel(Model model, String formAction, String pageTitle, String submitLabel) {
        model.addAttribute("formAction", formAction);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("submitLabel", submitLabel);
    }
}
