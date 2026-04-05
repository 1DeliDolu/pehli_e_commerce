package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.ProductForm;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.service.CategoryService;
import com.pehlione.kafka.service.ProductService;
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
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CommerceEventPublisher commerceEventPublisher;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        fillFormModel(model, "/admin/products", "Yeni Urun", "Urunu Kaydet");
        return "admin/products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            fillFormModel(model, "/admin/products", "Yeni Urun", "Urunu Kaydet");
            return "admin/products/form";
        }

        try {
            Product product = productService.create(form);
            commerceEventPublisher.publishAdminProductEvent("product.created", authentication.getName(), product);
            redirectAttributes.addFlashAttribute("successMessage", "Urun olusturuldu.");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formErrorMessage", exception.getMessage());
            fillFormModel(model, "/admin/products", "Yeni Urun", "Urunu Kaydet");
            return "admin/products/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setCategoryId(product.getCategory().getId());
        model.addAttribute("productForm", form);
        fillFormModel(model, "/admin/products/" + id, "Urun Duzenle", "Degisiklikleri Kaydet");
        return "admin/products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            fillFormModel(model, "/admin/products/" + id, "Urun Duzenle", "Degisiklikleri Kaydet");
            return "admin/products/form";
        }

        try {
            Product product = productService.update(id, form);
            commerceEventPublisher.publishAdminProductEvent("product.updated", authentication.getName(), product);
            redirectAttributes.addFlashAttribute("successMessage", "Urun guncellendi.");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formErrorMessage", exception.getMessage());
            fillFormModel(model, "/admin/products/" + id, "Urun Duzenle", "Degisiklikleri Kaydet");
            return "admin/products/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            productService.delete(id);
            commerceEventPublisher.publishAdminProductEvent("product.deleted", authentication.getName(), product);
            redirectAttributes.addFlashAttribute("successMessage", "Urun silindi.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/products";
    }

    private void fillFormModel(Model model, String formAction, String pageTitle, String submitLabel) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("formAction", formAction);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("submitLabel", submitLabel);
    }
}
